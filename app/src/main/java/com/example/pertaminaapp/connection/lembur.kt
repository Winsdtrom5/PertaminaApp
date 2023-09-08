package com.example.pertaminaapp.connection

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object lembur {
    private val DB_URL = "jdbc:mysql://sql.freedb.tech:3306/freedb_lembur"
    private val DB_USER = "freedb_Windstrom7"
    private val DB_PASSWORD = "9b?Pk4g2NVc/Z\$Dt#"

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