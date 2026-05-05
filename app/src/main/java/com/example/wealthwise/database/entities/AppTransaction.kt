package com.example.wealthwise.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class AppTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "", // Added for multi-user support
    val amount: Double,
    val date: String,
    val categoryId: Int,
    val description: String = "",
    val type: String,
    val photoPath: String? = null
)
