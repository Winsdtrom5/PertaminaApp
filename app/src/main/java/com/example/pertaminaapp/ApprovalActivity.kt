package com.example.pertaminaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pertaminaapp.databinding.ActivityApprovalBinding
import com.example.pertaminaapp.databinding.ActivityMainBinding
import com.example.pertaminaapp.session.SessionManager

class ApprovalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApprovalBinding
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        // Initialize com.example.pertaminaapp.session.SessionManager
        sessionManager = SessionManager(applicationContext)

        // Check if the user is logged in with a valid token
        if (sessionManager.checkLogin() && sessionManager.hasValidToken()) {
            // Token is valid, auto-login the user
            // You may also perform a token refresh if needed
            autoLogin()
        } else {
            // The user is not logged in or the token has expired
            // Redirect to the login activity
            startActivity(Intent(this@ApprovalActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun autoLogin() {
        // Perform auto-login logic using the stored token
        // You can make an authenticated API request to verify the token and retrieve user data
        // If successful, proceed with the user's session
        // If not, redirect to the login activity

        // Example:
        // val token = sessionManager.getToken()
        // val loggedIn = performTokenValidation(token)

        // if (loggedIn) {
        //     // Proceed with the user's session
        // } else {
        //     // Token validation failed, redirect to the login activity
        //     startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        //     finish()
        // }
    }
}