package com.example.wealthwise.database.dao

import androidx.room.*
import com.example.wealthwise.database.entities.User

@Dao
interface UserDao {

    // Find by username OR email — used for login
    @Query("SELECT * FROM users WHERE username = :identifier OR email = :identifier LIMIT 1")
    suspend fun findByIdentifier(identifier: String): User?

    // Check if username already taken — used during sign up
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun usernameExists(username: String): Int

    // Insert new user — returns the new row ID
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    // Update user — used for password change
    @Update
    suspend fun update(user: User)

    // Get user by ID
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?
}