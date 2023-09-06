package com.example.pertaminaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.example.pertaminaapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var username : String
    private lateinit var password : String
    private lateinit var binding: ActivityMainBinding
    private lateinit var loading : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()?.hide()
    }

    private fun login(Username : String,Password : String){

    }
}