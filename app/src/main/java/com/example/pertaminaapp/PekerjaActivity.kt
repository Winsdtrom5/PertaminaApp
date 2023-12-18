package com.example.pertaminaapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.databinding.ActivityPekerjaBinding
import com.example.pertaminaapp.model.Holiday
import com.example.pertaminaapp.model.HolidayList
import com.example.pertaminaapp.model.User
import com.example.pertaminaapp.session.SessionManager
import com.example.pertaminaapp.session.SharedPreferencesManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Calendar


class PekerjaActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding : ActivityPekerjaBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var drawer : DrawerLayout
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView : NavigationView
    private lateinit var loading : LinearLayout
    private lateinit var user: User
    private lateinit var mbunlde : Bundle
    private lateinit var setuju:TextView
    private lateinit var tolak : TextView
    private lateinit var revisi: TextView
    private lateinit var tunda : TextView
    private lateinit var holidayList: HolidayList
    private lateinit var navbar: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPekerjaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.nav_view)
        drawer =findViewById(R.id.drawer_layout)
        setSupportActionBar(toolbar);
        loading = findViewById(R.id.layout_loading)
        val headerView = navigationView.getHeaderView(0)
        navbar = headerView.findViewById(R.id.navbar)
        getBundle()
        navigationView.setNavigationItemSelectedListener(this)
//        startRealTimeUpdates()
        setDashboard(user)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                // If the drawer is open, close it and rotate the menu icon back to 0 degrees
                drawer.closeDrawer(GravityCompat.START)
                rotateMenuIcon(false)
            } else {
                // If the drawer is closed, open it and rotate the menu icon to 90 degrees
                drawer.openDrawer(GravityCompat.START)
                rotateMenuIcon(true)
            }
        }
        toggle = ActionBarDrawerToggle(this,drawer,toolbar,
            R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
//        navbar.setOnClickListener {
//            val intent = Intent(this,ProfileActivity::class.java)
//            val userBundle = Bundle()
//            userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
//            intent.putExtra("user_bundle", userBundle)
//            startActivity(intent)
//        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        when (item.itemId) {
            R.id.menupanduan -> {
                val pdfFileName = "panduan_eworks.pdf"

                // Create a DownloadManager request
                val request = DownloadManager.Request(Uri.parse("android.resource://${packageName}/raw/${pdfFileName}"))

                // Set the title for the notification (visible in the downloads UI)
                request.setTitle("Downloading Panduan eWorks PDF")

                // Set the description for the notification
                request.setDescription("Downloading...")

                // Set the local destination for the downloaded file
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pdfFileName)

                // Get the DownloadManager service
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                // Enqueue the download request
                val downloadId = downloadManager.enqueue(request)
                Toast.makeText(this, "Downloading Panduan eWorks PDF...", Toast.LENGTH_SHORT).show()
            }
            R.id.menuHome -> {
                // Handle the "Home" item click (replace with your desired activity)
                val intent = Intent(this@PekerjaActivity, PekerjaActivity::class.java)
                val userBundle = Bundle()

                userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
                intent.putExtra("user_bundle", userBundle)
                intent.putExtra("$currentYear", holidayList)
                startActivity(intent)
            }
            R.id.menulembur -> {
                Log.d("Test","Clicked")
                // Handle the "Lembur" item click (replace with your desired activity)
                val intent = Intent(this@PekerjaActivity, LemburActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
                intent.putExtra("user_bundle", userBundle)
                intent.putExtra("$currentYear", holidayList)
                startActivity(intent)
            }
            R.id.menudinas -> {
                Log.d("Test","Clicked")
                // Handle the "Lembur" item click (replace with your desired activity)
                val intent = Intent(this@PekerjaActivity, DinasActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
                intent.putExtra("user_bundle", userBundle)
                intent.putExtra("$currentYear", holidayList)
                startActivity(intent)
            }
            R.id.menulogout -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@PekerjaActivity)
                builder.setMessage("Want to log out?")
                    .setNegativeButton("No", object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface, i: Int) {

                        }
                    })
                    .setPositiveButton("YES", object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface, i: Int) {
                            // Get an instance of your SharedPreferencesManager
                            val sharedPreferencesManager = SharedPreferencesManager(this@PekerjaActivity)

                            // Clear user data (and other data if needed)
                            sharedPreferencesManager.clearUserData()

                            // Navigate to the MainActivity or login screen
                            startActivity(Intent(this@PekerjaActivity, MainActivity::class.java))
                        }
                    })
                    .show()
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }
    private fun getBundle() {
        try {
            mbunlde = intent?.getBundleExtra("user_bundle")!!
            if (mbunlde != null) {
                user = mbunlde.getParcelable("user")!!
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val receivedHolidayList = intent.getParcelableExtra<HolidayList>("$currentYear")

            // Check if the receivedHolidayList is not null
            if (receivedHolidayList != null) {
                holidayList = receivedHolidayList
            } else {
                // If the holidayList is null, fetch it from your data source
                holidayList = getHoliday()
            }
        } catch (e: NullPointerException) {
            // Handle the case where the bundle or user object is not found
        }
    }


    private fun getHoliday(): HolidayList {
        val holidays = mutableListOf<Holiday>()

        try {
            GlobalScope.launch(Dispatchers.IO) {
                // Use your database access code here
                val connection = eworks.getConnection()
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
        return HolidayList(holidays)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@PekerjaActivity)
        builder.setMessage("Want to log out?")
            .setNegativeButton("No", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {

                }
            })
            .setPositiveButton("YES", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    startActivity(Intent(this@PekerjaActivity, MainActivity::class.java))
                }
            })
            .show()
    }
//    private fun updateSignalStrength(navigationView: NavigationView, signalStrengthLevel: Int,ping : String) {
//        val navigationView = findViewById<NavigationView>(R.id.nav_view)
//        val headerView = navigationView.getHeaderView(0)
//        val pingTextView = headerView.findViewById<TextView>(R.id.ping_show)
//        val signalStrengthBar = headerView.findViewById<View>(R.id.signal_strength_bar)
//        pingTextView.text = "Ping: $ping"
//
//        val barSize = resources.getDimensionPixelSize(R.dimen.signal_strength_bar_size)
//        val barColor = when (signalStrengthLevel) {
//            0 -> Color.RED
//            1 -> Color.YELLOW
//            2 -> Color.GREEN
//            else -> Color.BLUE
//        }
//
//        val layoutParams = signalStrengthBar.layoutParams
//        layoutParams.width = barSize
//        layoutParams.height = barSize
//        signalStrengthBar.layoutParams = layoutParams
//        signalStrengthBar.setBackgroundColor(barColor)
//    }
//    private fun startRealTimeUpdates() {
//        GlobalScope.launch(Dispatchers.Main) {
//            while (true) {
//                // Simulated values (replace with your actual data sources)
//                val signalStrengthLevel = Random.nextInt(0, 4)
//                val pingMs = "${Random.nextInt(20, 200)} ms"
//                val isOnline = Random.nextBoolean()
//
//                // Update username, ping, and signal strength in real-time
//                updateSignalStrength(navigationView, signalStrengthLevel,pingMs)
//
//                delay(5000) // Update every 5 seconds (adjust as needed)
//            }
//        }
//    }
    @SuppressLint("ObjectAnimatorBinding")
    private fun rotateMenuIcon(open: Boolean) {
        val rotation = if (open) 90f else 0f // Rotate 90 degrees when the drawer is opened

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val menuIcon = toolbar.navigationIcon

        val rotationAnimator = ObjectAnimator.ofFloat(menuIcon, "rotation", rotation)
        rotationAnimator.duration = 250 // Adjust the duration as needed

        val animatorSet = AnimatorSet()
        animatorSet.play(rotationAnimator)
        animatorSet.start()
    }

//    private fun dashboard(user: User){
//        setuju.text = user.approveCount
//        tolak.text = user.rejectedCount
//        revisi.text = user.returnCount
//        tunda.text = user.pendingCount
//        Log.d("User",user.approveCount)
//        setUsername(navigationView, user.nama)
//        setDashboard(user.kode)
//    }

    private fun setUsername(navigationView: NavigationView, username: String) {
        println("status: $username")
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.username_show)
        usernameTextView.text = username
    }

    private fun setDashboard(user: User) {
//        GlobalScope.launch(Dispatchers.IO) {
//            val connection = eworks.getConnection()
//            if (connection != null) {
//                try {
//                    val query = """
//                    SELECT status FROM spkl WHERE kode_pekerja = ?
//                    UNION ALL
//                    SELECT status FROM spd WHERE kode_pekerja = ?
//                """.trimIndent()
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
//                    while (resultSet.next()) {
//                        val status = resultSet.getString("status")
//                        when (status) {
//                            "Returned" -> returnCount++
//                            "Diverted" -> divertedCount++
//                            "Rejected" -> rejectedCount++
//                            "Pending" -> pendingCount++
//                            "Review" -> reviewCount++
//                            else -> approveCount++
//                        }
//                    }
//
//                    // Update UI on the main thread
//                    runOnUiThread {
//                        setuju.text = approveCount.toString()
//                        tolak.text = rejectedCount.toString()
//                        revisi.text = returnCount.toString()
//                        tunda.text = pendingCount.toString()
//                    }
//
//                } catch (e: SQLException) {
//                    e.printStackTrace()
//                } finally {
//                    // Close the connection in a finally block
//                    try {
//                        connection.close()
//                    } catch (e: SQLException) {
//                        e.printStackTrace()
//                    }
//                }
//            } else {
//                runOnUiThread {
//                    Toast.makeText(this@PekerjaActivity, "Tidak dapat tersambung", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

        val namaTV = binding.nama
        val pendidikanTV = binding.jurusan
        val lama = binding.masa
        val namaProfile = binding.namaAkun
        val umur = binding.umur
        val klasifikasiTV = binding.klasifikasi
        val kode = binding.kodePekerja
        val polaTV = binding.pola
        val pjpTV = binding.pjp
        val fungsiTV = binding.fungsi
        val birth = binding.tgl
        val genderTV = binding.gender
        val pekerjaan = binding.pekerjaan
        namaTV.setText(user.nama)
        pendidikanTV.setText(user.pendidikan)
        lama.setText(user.masaKerja)
        genderTV.setText(user.gender)
        namaProfile.setText(user.nama)
        birth.setText(user.tgl_lahir)
        umur.setText(user.age)
        fungsiTV.setText(user.fungsi)
        klasifikasiTV.setText(user.klasifikasi)
        polaTV.setText(user.pola)
        pjpTV.setText(user.pjp)
        kode.setText(user.kode)
        pekerjaan.setText(user.jabatan)
        setUsername(navigationView, user.nama)
    }

    private fun getName(kode:String){
        GlobalScope.launch(Dispatchers.IO) {
            // Check the username and password in the database
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query = "SELECT * FROM biodata WHERE kode_pekerja = ?"
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, kode)
                    val resultSet: ResultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        val nama = resultSet.getString("nama")
                        Log.d("test",nama)
                        setUsername(navigationView, nama)
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@PekerjaActivity, "Failed Connect To Database", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@PekerjaActivity,"Tidak dapat tersambung",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(isLoading:Boolean){
        if(isLoading){
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            loading!!.visibility = View.VISIBLE
        }else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            loading!!.visibility = View.INVISIBLE
        }
    }
}