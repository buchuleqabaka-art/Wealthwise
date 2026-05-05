package com.example.wealthwise.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.wealthwise.SessionManager
import com.example.wealthwise.database.WealthWiseDatabase
import com.example.wealthwise.database.dao.CategoryDao
import com.example.wealthwise.database.entities.*
import com.example.wealthwise.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CategoryTotal(
    val name: String,
    val amount: Double,
    val color: String
)

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WealthWiseDatabase.getDatabase(application)
    private val categoryDao: CategoryDao = db.categoryDao()
    private val repository: TransactionRepository = TransactionRepository(db.transactionDao())

    // 1. Core State: User ID
    private val userIdLiveData = MutableLiveData<String>()

    // 2. Filter State
    private val categoryTypeFilter = MutableLiveData("EXPENSE")

    init {
        refreshUser()
    }

    // 3. Derived Data (User-scoped)
    
    val allTransactions: LiveData<List<AppTransaction>> = userIdLiveData.switchMap { uid ->
        repository.getAllTransactions(uid)
    }

    val allTransactionsWithCategory: LiveData<List<TransactionWithCategory>> = userIdLiveData.switchMap { uid ->
        repository.getAllTransactionsWithCategory(uid)
    }

    val allCategories: LiveData<List<Category>> = userIdLiveData.switchMap { uid ->
        categoryDao.getAllCategories(uid)
    }

    val expenseCategories: LiveData<List<Category>> = userIdLiveData.switchMap { uid ->
        categoryDao.getExpenseCategories(uid)
    }

    val filteredCategories: LiveData<List<Category>> = userIdLiveData.switchMap { uid ->
        categoryTypeFilter.switchMap { type ->
            categoryDao.getCategoriesByType(uid, type)
        }
    }

    val totalIncome: LiveData<Double> = userIdLiveData.switchMap { uid ->
        repository.getTotalIncome(uid)
    }

    val totalExpenses: LiveData<Double> = userIdLiveData.switchMap { uid ->
        repository.getTotalExpenses(uid)
    }

    val balance: LiveData<Double> = userIdLiveData.switchMap { uid ->
        val income = repository.getTotalIncome(uid)
        val expenses = repository.getTotalExpenses(uid)
        MediatorLiveData<Double>().apply {
            fun update() {
                val inc = income.value ?: 0.0
                val exp = expenses.value ?: 0.0
                value = inc - exp
            }
            addSource(income) { update() }
            addSource(expenses) { update() }
        }
    }

    val categoryTotalsInfo: LiveData<List<CategoryTotal>> = userIdLiveData.switchMap { uid ->
        val transactions = repository.getAllTransactions(uid)
        val categories = categoryDao.getAllCategories(uid)
        MediatorLiveData<List<CategoryTotal>>().apply {
            fun update() {
                val trans = transactions.value ?: return
                val cats = categories.value ?: return
                value = computeCategoryTotalsInfo(trans, cats)
            }
            addSource(transactions) { update() }
            addSource(categories) { update() }
        }
    }

    val budgetData: LiveData<List<com.example.wealthwise.ui.budget.BudgetCategory>> = userIdLiveData.switchMap { uid ->
        val categories = categoryDao.getExpenseCategories(uid)
        val transactions = repository.getAllTransactions(uid)
        MediatorLiveData<List<com.example.wealthwise.ui.budget.BudgetCategory>>().apply {
            fun update() {
                val cats = categories.value ?: return
                val trans = transactions.value ?: return
                value = computeBudgetData(cats, trans)
            }
            addSource(categories) { update() }
            addSource(transactions) { update() }
        }
    }

    val monthlyDailySpending: LiveData<List<DateSum>> = userIdLiveData.switchMap { uid ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        repository.getDailyExpense(uid, startDate)
    }

    // 4. Operations (Always enforce current userId)

    fun insert(transaction: AppTransaction) = viewModelScope.launch {
        repository.insert(transaction.copy(userId = getUserId()))
    }

    fun update(transaction: AppTransaction) = viewModelScope.launch {
        repository.update(transaction.copy(userId = getUserId()))
    }

    fun delete(transaction: AppTransaction) = viewModelScope.launch {
        repository.delete(transaction.copy(userId = getUserId()))
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        categoryDao.insert(category.copy(userId = getUserId()))
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryDao.delete(category.copy(userId = getUserId()))
    }

    fun setCategoryTypeFilter(type: String) {
        categoryTypeFilter.value = type
    }

    fun refreshUser() {
        userIdLiveData.value = getUserId()
    }

    private fun getUserId(): String {
        return SessionManager.getUserId(getApplication()) ?: ""
    }

    // 5. Pure Computations

    private fun computeBudgetData(categories: List<Category>, transactions: List<AppTransaction>): List<com.example.wealthwise.ui.budget.BudgetCategory> {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val startOfMonth = "%04d-%02d-01".format(year, month)

        val categorySpentMap = transactions
            .filter { it.type == "EXPENSE" && it.date >= startOfMonth }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        return categories.map { cat ->
            com.example.wealthwise.ui.budget.BudgetCategory(cat, categorySpentMap[cat.id] ?: 0.0)
        }
    }

    private fun computeCategoryTotalsInfo(transactions: List<AppTransaction>, categories: List<Category>): List<CategoryTotal> {
        val categoryMap = categories.associateBy { it.id }

        return transactions.asSequence()
            .filter { it.type == "EXPENSE" }
            .groupBy { it.categoryId }
            .map { (catId, transList) ->
                val cat = categoryMap[catId]
                CategoryTotal(
                    name = cat?.name ?: "Other",
                    amount = transList.sumOf { it.amount },
                    color = cat?.color ?: "#7BA7D4"
                )
            }
            .sortedByDescending { it.amount }
            .toList()
    }
}
