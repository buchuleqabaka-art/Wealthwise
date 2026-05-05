package com.example.wealthwise.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "", // Changed to String for Firebase UID
    val name: String,
    val targetAmount: Double,    // maximum goal
    val minGoal: Double = 0.0,   // minimum goal
    val savedAmount: Double = 0.0,
    val targetDate: String? = null,
    val badgeAwarded: Boolean = false
)
