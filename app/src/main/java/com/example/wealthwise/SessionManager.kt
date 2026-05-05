package com.example.wealthwise

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME       = "wealthwise_session"
    private const val KEY_USER_ID     = "logged_in_user_id"
    private const val KEY_USER_NAME   = "logged_in_user_name"
    private const val KEY_USER_EMAIL  = "logged_in_user_email"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Call this when login succeeds. 
     * Stored in "wealthwise_session" to keep legacy SessionManager functionality
     * but also updates "wealthwise_prefs" for compatibility with WelcomeFragment.
     */
    fun login(context: Context, userId: String, fullName: String, email: String) {
        // Update SessionManager specific prefs
        prefs(context).edit()
            .putString(KEY_USER_ID,    userId)
            .putString(KEY_USER_NAME,  fullName)
            .putString(KEY_USER_EMAIL, email)
            .apply()

        // Sync with what WelcomeFragment expects
        context.getSharedPreferences("wealthwise_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("logged_in_name", fullName)
            .apply()
    }

    // Call this when user logs out
    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
        context.getSharedPreferences("wealthwise_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun getUserId(context: Context): String? =
        prefs(context).getString(KEY_USER_ID, null)

    fun getUserName(context: Context): String =
        prefs(context).getString(KEY_USER_NAME, "User") ?: "User"

    fun getUserEmail(context: Context): String =
        prefs(context).getString(KEY_USER_EMAIL, "") ?: ""

    // Check if anyone is logged in
    fun isLoggedIn(context: Context): Boolean =
        getUserId(context) != null
}
