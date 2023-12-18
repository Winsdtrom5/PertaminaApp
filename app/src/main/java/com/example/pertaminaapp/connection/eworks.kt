package com.example.pertaminaapp.connection

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object eworks {
    private val DB_URL = "jdbc:mysql://192.168.1.10:3306/eworks2"
    private val DB_USER = "Angga"
    private val DB_PASSWORD = "Angga123456"

    suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        try {
            Class.forName("com.mysql.jdbc.Driver") // Use the MySQL JDBC driver class

            // Set up the connection using DriverManager
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        connection
    }
}