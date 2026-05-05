package com.example.wealthwise.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.wealthwise.database.entities.SavingsGoal

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY name ASC")
    fun getAllGoals(userId: String): LiveData<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal)

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)
}
