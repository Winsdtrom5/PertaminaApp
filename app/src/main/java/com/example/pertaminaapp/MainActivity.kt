package com.example.pertaminaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.connection.eworks.getConnection
import com.example.pertaminaapp.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var login : Button
    private lateinit var user : TextInputLayout
    private lateinit var pass : TextInputLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        login = binding.lgnbtn
        user = binding.tl1
        pass = binding.tl2
        login.setOnClickListener{
            val username = user.editText?.text.toString()
            val password = pass.editText?.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Launch a coroutine to handle database access
                GlobalScope.launch(Dispatchers.IO) {
                    val md5Password = hashWithMD5(password) // Convert the password to MD5 hash

                    // Check the username and password in the database
                    val connection = getConnection()
                    if (connection != null) {
                        try {
                            val query = "SELECT * FROM user WHERE username = ? AND password = ?"
                            val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                            preparedStatement.setString(1, username)
                            preparedStatement.setString(2, md5Password)
                            val resultSet: ResultSet = preparedStatement.executeQuery()

                            if (resultSet.next()) {
                                val intent = Intent(this@MainActivity,AtasanActivity::class.java)
                                startActivity(intent)
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "Username atau password salah", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: SQLException) {
                            e.printStackTrace()
                        } finally {
                            // Close the connection in a finally block
                            try {
                                connection.close()
                            } catch (e: SQLException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        Toast.makeText(this@MainActivity,"Tidak dapat tersambung",Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Handle empty username or password fields
            }
        }
    }
    private fun hashWithMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        val result = StringBuilder()
        for (b in digest) {
            result.append(String.format("%02x", b))
        }
        return result.toString()
    }
}

