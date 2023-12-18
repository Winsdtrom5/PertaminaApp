package com.example.pertaminaapp.session

import android.content.Context
import android.content.SharedPreferences
import com.example.pertaminaapp.model.User
import com.example.pertaminaapp.model.Reviewer
import com.google.gson.Gson

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val userJson = gson.toJson(user)
        editor.putString("user", userJson)
        editor.apply()
    }

    fun getUser(): User? {
        val gson = Gson()
        val userJson = sharedPreferences.getString("user", null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.remove("user")
        editor.remove("reviewer") // Add more data to remove if needed
        // Add more editor.remove(...) calls to remove other data as needed
        editor.apply()
    }

    fun saveReviewer(reviewer: Reviewer) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val reviewerJson = gson.toJson(reviewer)
        editor.putString("reviewer", reviewerJson)
        editor.apply()
    }

    fun getReviewer(): Reviewer? {
        val gson = Gson()
        val reviewerJson = sharedPreferences.getString("reviewer", null)
        return if (reviewerJson != null) {
            gson.fromJson(reviewerJson, Reviewer::class.java)
        } else {
            null
        }
    }
}
