package com.example.wealthwise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wealthwise.database.dao.CategoryDao
import com.example.wealthwise.database.dao.SavingsGoalDao
import com.example.wealthwise.database.dao.TransactionDao
import com.example.wealthwise.database.dao.UserDao
import com.example.wealthwise.database.entities.Category
import com.example.wealthwise.database.entities.SavingsGoal
import com.example.wealthwise.database.entities.AppTransaction
import com.example.wealthwise.database.entities.User

@Database(
    entities = [
        User::class,
        AppTransaction::class,
        Category::class,
        SavingsGoal::class
    ],
    version = 3, // Incrementing version again
    exportSchema = false
)
abstract class WealthWiseDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: WealthWiseDatabase? = null

        fun getDatabase(context: Context): WealthWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WealthWiseDatabase::class.java,
                    "wealthwise_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
