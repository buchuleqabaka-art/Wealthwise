package com.example.wealthwise.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithCategory(
    @Embedded val transaction: AppTransaction,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id",
        entity = Category::class
    )
    val category: Category?
)
