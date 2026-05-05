package com.example.wealthwise.repository

import androidx.lifecycle.LiveData
import com.example.wealthwise.database.dao.TransactionDao
import com.example.wealthwise.database.entities.AppTransaction
import com.example.wealthwise.database.entities.DateSum
import com.example.wealthwise.database.entities.TransactionWithCategory

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getAllTransactions(userId: String): LiveData<List<AppTransaction>> = 
        transactionDao.getAllTransactions(userId)

    fun getAllTransactionsWithCategory(userId: String): LiveData<List<TransactionWithCategory>> = 
        transactionDao.getAllTransactionsWithCategory(userId)

    fun getTotalIncome(userId: String): LiveData<Double> = 
        transactionDao.getTotalIncome(userId)

    fun getTotalExpenses(userId: String): LiveData<Double> = 
        transactionDao.getTotalExpenses(userId)

    fun getDailyExpense(userId: String, startDate: String): LiveData<List<DateSum>> {
        return transactionDao.getDailyExpense(userId, startDate)
    }

    suspend fun insert(transaction: AppTransaction) {
        transactionDao.insert(transaction)
    }

    suspend fun update(transaction: AppTransaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: AppTransaction) {
        transactionDao.delete(transaction)
    }
}
