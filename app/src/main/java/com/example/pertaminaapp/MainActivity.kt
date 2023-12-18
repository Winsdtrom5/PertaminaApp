package com.example.pertaminaapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.connection.eworks.getConnection
import com.example.pertaminaapp.databinding.ActivityMainBinding
import com.example.pertaminaapp.model.DinasData
import com.example.pertaminaapp.model.Holiday
import com.example.pertaminaapp.model.HolidayList
import com.example.pertaminaapp.model.LemburData
import com.example.pertaminaapp.model.Reviewer
import com.example.pertaminaapp.model.User
import com.example.pertaminaapp.session.SharedPreferencesManager
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var login : Button
    private lateinit var user : TextInputLayout
    private lateinit var pass : TextInputLayout
    private lateinit var loading : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        login = binding.lgnbtn
        user = binding.tl1
        pass = binding.tl2
        // Creating a list of holidays
        loading = findViewById(R.id.layout_loading)
        val sharedPreferencesManager = SharedPreferencesManager(this)
        // Check if a user is already logged in
        val savedUser = sharedPreferencesManager.getUser()
        val savedReviewer = sharedPreferencesManager.getReviewer()

        if (savedUser != null) {
            // A user is already logged in, redirect to the appropriate activity
            redirectToActivity(savedUser)
            return // Exit onCreate to prevent further execution
        } else if (savedReviewer != null) {
            // A reviewer is already saved, redirect to the appropriate activity
            redirectToReviewerActivity(savedReviewer)
            return // Exit onCreate to prevent further execution
        }

        login.setOnClickListener{
            val username = user.editText?.text.toString()
            val password = pass.editText?.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Launch a coroutine to handle database access
                setLoading(true)
                val userCache = ConcurrentHashMap<String, User>()
                GlobalScope.launch(Dispatchers.IO) {
                    val md5Password = hashWithMD5(password) // Convert the password to MD5 hash
                    Log.d("Done","A")
                    // Check the username and password in the database
                    val connection = getConnection()
                    if (connection != null) {
                        try {
                            val query = """SELECT * from user WHERE username = ? AND password = ?""".trimIndent()
                            val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                            preparedStatement.setString(1, username)
                            preparedStatement.setString(2, md5Password)
                            val resultSet: ResultSet = preparedStatement.executeQuery()
                            if (resultSet.next()) {
                                val jenis = resultSet.getString("level_user")
                                val keaktifan = resultSet.getString("keaktifan")
                                if(jenis == "Reviewer" && keaktifan == "aktif"){
                                    try{
                                        val query2 = """
                                        Select c.nama_unit_org,d.nama,d.posisi,d.email FROM master_unit as c
                                        JOIN reviewer as d ON c.id_org_unit=d.id_org_unit where d.no_pers=?""".trimIndent()
                                        val preparedStatement2: PreparedStatement = connection.prepareStatement(query2)
                                        preparedStatement2.setString(1, username)
                                        val resultSet2: ResultSet = preparedStatement2.executeQuery()
                                        if (resultSet2.next()) {
                                            // Convert the holidayList to an ArrayList
                                            val posisi = resultSet2.getString("posisi")
                                            val nama = resultSet2.getString("nama")
                                            val email = resultSet2.getString("email")
                                            val reviewer = Reviewer(
                                                username,
                                                nama, // You can set the name field as empty for now
                                                email,
                                                posisi
                                            )
                                            // Store user data in cache
                                            runOnUiThread {
                                                setLoading(false)
                                            }
                                             val intent =
                                                Intent(
                                                    this@MainActivity,
                                                    ReviewActivity::class.java
                                                )
                                            sharedPreferencesManager.saveReviewer(reviewer)
                                            val userBundle = Bundle()
                                            userBundle.putParcelable("user", reviewer) // Serialize the user object to a Bundle
                                            intent.putExtra("user_bundle", userBundle)
                                            startActivity(intent)
                                        }
                                    } catch (e: SQLException) {
                                        Log.d("Done","Ambatukam")
                                        e.printStackTrace()
                                        runOnUiThread {
                                            setLoading(false)
                                            Toast.makeText(this@MainActivity, "Error Has Occured", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }else if(jenis == "Pekerja"&& keaktifan == "aktif"){
                                    try {
                                        val query2 = """
                                          SELECT a.kode_pekerja,a.pola_kerja, a.klasifikasi_name, a.kota_kerja, a.lokasi,
                                          a.nama, a.PJP, a.pendidikan, a.tanggal_lahir, a.jurusan, a.jenis_kelamin, a.masa_kerja,
                                          a.fungsi_pengguna,a.upah,b.nama_unit_org,c.email FROM biodata AS a JOIN master_unit AS b ON a.cost_center_pengguna=b.cost_center_pengguna
                                          JOIN reviewer AS c ON b.id_org_unit=c.id_org_unit WHERE a.kode_pekerja  = ?""".trimIndent()
                                        val preparedStatement2: PreparedStatement = connection.prepareStatement(query2)
                                        preparedStatement2.setString(1, username)
                                        val resultSet2: ResultSet = preparedStatement2.executeQuery()
                                        if (resultSet2.next()) {
                                            val holidayList = getHoliday()
                                            // Convert the holidayList to an ArrayList
                                            val holidayListParcelable = ArrayList(holidayList)
                                            val nama = resultSet2.getString("nama")
                                            val tanggal = resultSet2.getString("tanggal_lahir")
                                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                            val birthDate = LocalDate.parse(tanggal, dateFormatter)
                                            val currentDate = LocalDate.now()
                                            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id-ID"))
                                            val tgl_lahir = birthDate.format(formatter)
                                            val ageformat = Period.between(birthDate, currentDate).years
                                            val age = "$ageformat Tahun"
                                            val jurusan = resultSet2.getString("jurusan")
                                            val gender = resultSet2.getString("jenis_kelamin")
                                            val masaKerja = resultSet2.getString("masa_kerja")
                                            val formattedMasaKerja = "$masaKerja Tahun"
                                            val fungsi = resultSet2.getString("fungsi_pengguna")
                                            val tamat = resultSet2.getString("pendidikan")
                                            val pendidikan = "$tamat $jurusan"
                                            val pola = resultSet2.getString("pola_kerja")
                                            val pjp = resultSet2.getString("PJP")
                                            val klasifikasi = resultSet2.getString("klasifikasi_name")
                                            val kota = resultSet2.getString("kota_kerja")
                                            val lokasi = resultSet2.getString("lokasi")
                                            val emailReview = resultSet2.getString("email")
                                            val jabatan = "$klasifikasi $lokasi $kota"
                                            val upah = resultSet2.getString("upah")
                                            val user = User(
                                                username,
                                                nama, // You can set the name field as empty for now
                                                pola,
                                                jenis,
                                                tgl_lahir,
                                                age,
                                                jurusan,
                                                gender,
                                                formattedMasaKerja,
                                                fungsi,
                                                pendidikan,
                                                pjp,
                                                klasifikasi,
                                                kota,
                                                lokasi,
                                                jabatan,
                                                emailReview,
                                                upah
                                            )
                                            runOnUiThread {
                                                setLoading(false)
                                            }
                                            val intent = Intent(this@MainActivity,PekerjaActivity::class.java)
                                            sharedPreferencesManager.saveUser(user)
                                            val userBundle = Bundle()
                                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                            userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
                                            intent.putExtra("user_bundle", userBundle)
                                            intent.putExtra("$currentYear", holidayListParcelable)
                                            startActivity(intent)
                                        }
                                    } catch (e: SQLException) {
                                        Log.d("Done","Ambatukam")
                                        e.printStackTrace()
                                        runOnUiThread {
                                            setLoading(false)
                                            Toast.makeText(this@MainActivity, "Error Has Occured", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }else if(keaktifan == "tidak aktif"){
                                    runOnUiThread {
                                        setLoading(false)
                                        Toast.makeText(this@MainActivity, "Member tidak aktif", Toast.LENGTH_SHORT).show()
                                    }
                                }else{
                                    runOnUiThread {
                                        setLoading(false)
                                    }
                                    val intent = Intent(this@MainActivity,AtasanActivity::class.java)
                                    val mBundle = Bundle()
                                    mBundle.putString("kode",username)
                                    intent.putExtra("user",mBundle)
                                    startActivity(intent)
                                }
                            } else {
                                runOnUiThread {
                                    setLoading(false)
                                    Toast.makeText(this@MainActivity, "Username atau password salah", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: SQLException) {
                            Log.d("Done","Ambatukam")
                            e.printStackTrace()
                            runOnUiThread {
                                setLoading(false)
                                Toast.makeText(this@MainActivity, "Error Has Occured", Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            // Close the connection in a finally block
                            try {
                                connection.close()
                            } catch (e: SQLException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        Log.d("P","Poii")
                        runOnUiThread {
                            setLoading(false)
                            Toast.makeText(this@MainActivity,"Tidak dapat tersambung",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@MainActivity,"Field belum terisi semua",Toast.LENGTH_SHORT).show()
                // Handle empty username or password fields
            }
        }
    }

    private suspend fun getHoliday(): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        try {
            withContext(Dispatchers.IO) {
                // Use your database access code here
                val connection = getConnection()
                if (connection != null) {
                    try {
                        val query = "SELECT * FROM libur WHERE YEAR(tanggal) = YEAR(CURDATE())"
                        val statement = connection.prepareStatement(query)
                        val resultSet = statement.executeQuery()

                        while (resultSet.next()) {
                            val tanggal = resultSet.getString("tanggal")
                            val nama = resultSet.getString("keterangan")
                            val holiday = Holiday(tanggal, nama)
                            holidays.add(holiday)
                        }
                    } finally {
                        connection.close()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Error", "Error fetching holidays: ${e.message}")
        }

        return holidays
    }
    private fun redirectToReviewerActivity(reviewer: Reviewer) {
        val intent = Intent(this, ReviewActivity::class.java)
        val userBundle = Bundle()
        userBundle.putParcelable("user", reviewer) // Serialize the user object to a Bundle
        intent.putExtra("user_bundle", userBundle)
        startActivity(intent)
        finish() // Finish the current activity to prevent the user from coming back to it using the back button
    }
    private fun redirectToActivity(user: User) {
        val intent: Intent
        // Determine which activity to redirect based on user's data
        if (user.jenis == "Reviewer") {
            intent = Intent(this@MainActivity, ApprovalActivity::class.java)
        } else if (user.jenis == "Pekerja") {
            intent = Intent(this@MainActivity, PekerjaActivity::class.java)
        } else {
            intent = Intent(this@MainActivity, AtasanActivity::class.java)
        }
        val userBundle = Bundle()
        userBundle.putParcelable("user", user)
        intent.putExtra("user_bundle", userBundle)
        startActivity(intent)
        finish() // Finish the current activity to prevent the user from coming back to it using the back button
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
//    private suspend fun getUserData(kode: String, nama: String,pola : String,jenis:String): User? {
//        return suspendCoroutine { continuation ->
//            GlobalScope.launch(Dispatchers.IO) {
//                val connection = getConnection()
//                if (connection != null) {
//                    try {
//                    val query = """
//                        SELECT
//                            SUM(CASE WHEN status = 'Returned' THEN 1 ELSE 0 END) AS returnCount,
//                            SUM(CASE WHEN status = 'Diverted' THEN 1 ELSE 0 END) AS divertedCount,
//                            SUM(CASE WHEN status = 'Rejected' THEN 1 ELSE 0 END) AS rejectedCount,
//                            SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) AS pendingCount,
//                            SUM(CASE WHEN status = 'Review' THEN 1 ELSE 0 END) AS reviewCount,
//                            SUM(CASE WHEN status NOT IN ('Returned', 'Diverted', 'Rejected', 'Pending', 'Review') THEN 1 ELSE 0 END) AS approveCount
//                        FROM (
//                            SELECT status FROM spkl WHERE kode_pekerja = ?
//                            UNION ALL
//                            SELECT status FROM spd WHERE kode_pekerja = ?
//                        ) AS combined_status
//                    """.trimIndent()
//
//                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
//                    preparedStatement.setString(1, kode)
//                    preparedStatement.setString(2, kode)
//
//                    val resultSet: ResultSet = preparedStatement.executeQuery()
//
//                    var approveCount = 0
//                    var divertedCount = 0
//                    var returnCount = 0
//                    var rejectedCount = 0
//                    var pendingCount = 0
//                    var reviewCount = 0
//
//                    if (resultSet.next()) {
//                        returnCount = resultSet.getInt("returnCount")
//                        divertedCount = resultSet.getInt("divertedCount")
//                        rejectedCount = resultSet.getInt("rejectedCount")
//                        pendingCount = resultSet.getInt("pendingCount")
//                        reviewCount = resultSet.getInt("reviewCount")
//                        approveCount = resultSet.getInt("approveCount")
//                    }
//                        val user = User(
//                            kode,
//                            nama,
//                            pola,
//                            jenis,
//                            approveCount.toString(),
//                            divertedCount.toString(),
//                            returnCount.toString(),
//                            rejectedCount.toString(),
//                            pendingCount.toString(),
//                            reviewCount.toString()
//                        )
//
//                        // Return the User object
//                        continuation.resume(user)
//                    } catch (e: SQLException) {
//                        e.printStackTrace()
//                        continuation.resume(null) // Handle the error case by returning null
//                    } finally {
//                        // Close the connection in a finally block
//                        try {
//                            connection.close()
//                        } catch (e: SQLException) {
//                            e.printStackTrace()
//                        }
//                    }
//                } else {
//                    runOnUiThread {
//                        Toast.makeText(this@MainActivity, "Tidak dapat tersambung", Toast.LENGTH_SHORT).show()
//                    }
//                    continuation.resume(null) // Handle the case when there is no connection
//                }
//            }
//        }
//    }

    private fun setLoading(isLoading:Boolean){
        if(isLoading){
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            pass.isEnabled=false
            loading!!.visibility = View.VISIBLE
        }else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            loading!!.visibility = View.INVISIBLE
            pass.isEnabled=true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Want to Close The App?")
            .setNegativeButton("No", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    // Do nothing or handle other actions
                }
            })
            .setPositiveButton("YES", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    // Close the current activity and return to MainActivity
                    finish()
                }
            })
            .show()
    }
}

