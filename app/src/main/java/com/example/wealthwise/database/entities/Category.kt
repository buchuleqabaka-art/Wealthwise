package com.example.wealthwise.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "", // Changed to String for Firebase UID compatibility
    val name: String,
    val type: String,            // "EXPENSE" or "INCOME"
    val iconName: String = "ic_category",
    val budgetLimit: Double = 0.0,
    val color: String = "#2563EB"
)
