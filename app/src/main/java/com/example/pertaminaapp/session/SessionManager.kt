package com.example.pertaminaapp.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "YourAppSession"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val AUTH_TOKEN = "authToken"
        private const val TOKEN_EXPIRATION = "tokenExpiration"
    }

    // Create a login session with a token and expiration time
    fun createLoginSession(token: String, expirationTimeMillis: Long) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(AUTH_TOKEN, token)
        editor.putLong(TOKEN_EXPIRATION, expirationTimeMillis)
        editor.apply()
    }

    // Check if the user is logged in
    fun checkLogin(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }

    // Get the stored authentication token
    fun getToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN, null)
    }

    // Check if the stored token is valid based on expiration time
    fun hasValidToken(): Boolean {
        val expirationTimeMillis = sharedPreferences.getLong(TOKEN_EXPIRATION, 0)
        val currentTimeMillis = System.currentTimeMillis()
        return expirationTimeMillis > currentTimeMillis
    }

    // Logout the user and clear the session
    fun logoutUser() {
        editor.clear()
        editor.apply()
    }
}
