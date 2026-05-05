package com.example.wealthwise.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.wealthwise.database.entities.AppTransaction
import com.example.wealthwise.database.entities.DateSum
import com.example.wealthwise.database.entities.TransactionWithCategory

@Dao
interface TransactionDao {

    // All transactions for this user
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): LiveData<List<AppTransaction>>

    //  Transactions with Categories
    @Transaction
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactionsWithCategory(userId: String): LiveData<List<TransactionWithCategory>>

    // Filter by date range — LiveData (for UI)
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsBetween(
        userId: String, startDate: String, endDate: String
    ): LiveData<List<AppTransaction>>

    //  Filter by date range — one-shot (for export)
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    suspend fun getTransactionsBetweenOnce(
        userId: String, startDate: String, endDate: String
    ): List<AppTransaction>

    // Totals
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND type = 'INCOME'
    """)
    fun getTotalIncome(userId: String): LiveData<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId AND type = 'EXPENSE'
    """)
    fun getTotalExpenses(userId: String): LiveData<Double>

    //  Category total (for budget bars)
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE userId = :userId 
        AND categoryId = :categoryId 
        AND type = 'EXPENSE'
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalForCategory(
        userId: String, categoryId: Int, startDate: String, endDate: String
    ): Double

    //  Category total by name (for goals)
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.userId = :userId
        AND c.name = :categoryName
        AND t.type = 'EXPENSE'
        AND t.date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalForCategoryName(
        userId: String, categoryName: String, startDate: String, endDate: String
    ): Double

    @Query("""
        SELECT date, SUM(amount) as total
        FROM transactions 
        WHERE userId = :userId 
        AND type = 'EXPENSE' 
        AND date >= :startDate 
        GROUP BY date 
        ORDER BY date ASC
    """)
    fun getDailyExpense(userId: String, startDate: String): LiveData<List<DateSum>>

    // Write operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: AppTransaction)

    @Update
    suspend fun update(transaction: AppTransaction)

    @Delete
    suspend fun delete(transaction: AppTransaction)
}
