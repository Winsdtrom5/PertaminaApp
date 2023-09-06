package com.example.pertaminaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.pertaminaapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var login : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        login = binding.lgnbtn
        login.setOnClickListener{
            val intent = Intent(this,AtasanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(Username:String,Password: String){
        
    }
}

