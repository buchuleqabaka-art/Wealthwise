package com.example.wealthwise.repository

import android.app.Application
import com.example.wealthwise.database.WealthWiseDatabase
import com.example.wealthwise.database.entities.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WealthWiseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Default categories are now seeded per-user during login/signup
        // seedDefaultCategories() 
    }

    // This can be called from LoginFragment or SignUpFragment
    companion object {
        fun seedDefaultCategories(context: android.content.Context, userId: String) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = WealthWiseDatabase.getDatabase(context)
                val dao = db.categoryDao()
                
                val existing = dao.getAllCategoriesOnce(userId)
                if (existing.isNotEmpty()) return@launch

                val defaultExpenseCategories = listOf(
                    Category(userId = userId, name = "Groceries",     type = "EXPENSE", budgetLimit = 3000.0, color = "#1D9E75"),
                    Category(userId = userId, name = "Transport",      type = "EXPENSE", budgetLimit = 1500.0, color = "#2563EB"),
                    Category(userId = userId, name = "Entertainment",  type = "EXPENSE", budgetLimit = 1000.0, color = "#534AB7"),
                    Category(userId = userId, name = "Clothing",       type = "EXPENSE", budgetLimit = 800.0,  color = "#E24B4A"),
                    Category(userId = userId, name = "Dining",         type = "EXPENSE", budgetLimit = 1200.0, color = "#EF9F27"),
                    Category(userId = userId, name = "Education",      type = "EXPENSE", budgetLimit = 500.0,  color = "#0F6E56"),
                    Category(userId = userId, name = "Health",         type = "EXPENSE", budgetLimit = 600.0,  color = "#D85A30"),
                    Category(userId = userId, name = "Utilities",      type = "EXPENSE", budgetLimit = 2000.0, color = "#185FA5")
                )

                val defaultIncomeCategories = listOf(
                    Category(userId = userId, name = "Salary",     type = "INCOME", color = "#1D9E75"),
                    Category(userId = userId, name = "Freelance",  type = "INCOME", color = "#2563EB"),
                    Category(userId = userId, name = "Business",   type = "INCOME", color = "#534AB7"),
                    Category(userId = userId, name = "Gifts",      type = "INCOME", color = "#EF9F27"),
                    Category(userId = userId, name = "Allowances", type = "INCOME", color = "#0F6E56"),
                    Category(userId = userId, name = "Rewards",    type = "INCOME", color = "#D85A30")
                )

                (defaultExpenseCategories + defaultIncomeCategories).forEach { category ->
                    dao.insert(category)
                }
            }
        }
    }
}
