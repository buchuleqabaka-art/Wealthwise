package com.example.wealthwise.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.wealthwise.database.entities.Category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getAllCategories(userId: String): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getAllCategoriesOnce(userId: String): List<Category>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type ORDER BY name ASC")
    fun getCategoriesByType(userId: String, type: String): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = 'EXPENSE' ORDER BY name ASC")
    fun getExpenseCategories(userId: String): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = 'INCOME' ORDER BY name ASC")
    fun getIncomeCategories(userId: String): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getCategoryById(id: Int, userId: String): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
