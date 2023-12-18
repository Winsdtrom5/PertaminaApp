package com.example.pertaminaapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.pertaminaapp.adapter.DinasItemAdapter
import com.example.pertaminaapp.adapter.LemburItemAdapter
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.databinding.ActivityReviewBinding
import com.example.pertaminaapp.model.DinasData
import com.example.pertaminaapp.model.DinasList
import com.example.pertaminaapp.model.HolidayList
import com.example.pertaminaapp.model.LemburData
import com.example.pertaminaapp.model.LemburList
import com.example.pertaminaapp.model.Reviewer
import com.example.pertaminaapp.model.User
import com.example.pertaminaapp.session.SessionManager
import com.example.pertaminaapp.session.SharedPreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Calendar


class ReviewActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityReviewBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var loading: LinearLayout
    private lateinit var user: Reviewer
    private lateinit var mbunlde: Bundle
    private lateinit var setuju: TextView
    private lateinit var DataDinas: DinasList
    private lateinit var DataLembur: LemburList
    private lateinit var tolak: TextView
    private lateinit var revisi: TextView
    private lateinit var tunda: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var lemburItemAdapter: LemburItemAdapter
    private lateinit var dinasItemAdapter: DinasItemAdapter
    private lateinit var navbar: LinearLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private var selectedMenuItemId: Int = R.id.menulembur
    private var isFirstTime = true
    private lateinit var textDataNotFound: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.nav_view)
        drawer = findViewById(R.id.drawer_layout)
        textDataNotFound = binding.notFound
        setSupportActionBar(toolbar);
        loading = findViewById(R.id.layout_loading)
        recyclerView = binding.recyclerView
        Log.d("Amkaming2","Amkaming2")
        val headerView = navigationView.getHeaderView(0)
        navbar = headerView.findViewById(R.id.navbar)
        getBundle()
        // Set the adapter for your RecyclerView
        navigationView.setNavigationItemSelectedListener(this)
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
        toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menulembur -> {
                    selectedMenuItemId = R.id.menulembur
                    refreshData()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menuDinas -> {
                    selectedMenuItemId = R.id.menuDinas
                    refreshData()
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true

            // If it's the first time, refresh outside the bottom navigation
            if (isFirstTime) {
                isFirstTime = false
                refreshData()
            } else {
                // Otherwise, let the bottom navigation handle the refresh
                swipeRefreshLayout.isRefreshing = false
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            // This is where the refresh operation happens
            refreshData()
        }
    }

    private fun refreshData() {
        // Fetch new data or perform necessary operations
        // For example, you can call the functions to update your data
        when (selectedMenuItemId) {
            R.id.menulembur -> {
                // Refresh Lembur data
                DataLembur = getDataLembur(user.nama)
                // Update your adapter or UI based on the new data
            }
            R.id.menuDinas -> {
                // Refresh Dinas data
                DataDinas = getDataDinas(user.nama)
                // Update your adapter or UI based on the new data
            }
        }

        // Initialize the adapter before setting it to the RecyclerView
        val extractedList: List<*> = when (selectedMenuItemId) {
            R.id.menulembur -> (DataLembur as? LemburList)?.LemburList ?: emptyList<LemburData>()
            R.id.menuDinas -> (DataDinas as? DinasList)?.DinasList ?: emptyList<DinasData>()
            else -> emptyList<Any>() // Handle other menu items if necessary
        }

        // Clear the RecyclerView data and reset the adapter
        recyclerView.adapter = null
        if (extractedList.isEmpty()) {

            textDataNotFound.visibility = View.VISIBLE
        } else {
            when (selectedMenuItemId) {
                R.id.menulembur -> {
                    lemburItemAdapter = LemburItemAdapter(extractedList as List<LemburData>, user)
                    recyclerView.adapter = lemburItemAdapter
                }

                R.id.menuDinas -> {
                    dinasItemAdapter = DinasItemAdapter(extractedList as List<DinasData>, user)
                    recyclerView.adapter = dinasItemAdapter
                }
                // Handle other menu items if necessary
            }
            // After the data is fetched or updated, stop the refreshing animation
            swipeRefreshLayout.isRefreshing = false
        }
    }


    private fun getDataLembur(username: String): LemburList {
        val lemburDataList = mutableListOf<LemburData>()
        GlobalScope.launch(Dispatchers.IO) {
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query = """
                    Select b.nomor,b.tanggal_pengajuan,b.kode_pekerja,a.nama,b.pekerjaan,
                    b.tanggal,b.posisi,b.mulai,b.akhir,b.uang_lembur,b.justifikasi FROM spkl as b join biodata as a ON a.kode_pekerja=b.kode_pekerja 
                    JOIN master_unit as c ON a.cost_center_pengguna=c.cost_center_pengguna 
                    JOIN reviewer as d ON c.id_org_unit=d.id_org_unit where d.no_pers=?
                    AND b.status='Pending'""".trimIndent()
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, username)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val data = LemburData(
                            resultSet.getString("nomor"),
                            resultSet.getString("tanggal_pengajuan"),
                            resultSet.getString("kode_pekerja"),
                            resultSet.getString("nama"),
                            resultSet.getString("pekerjaan"),
                            resultSet.getString("tanggal"),
                            resultSet.getString("posisi"),
                            resultSet.getString("mulai"),
                            resultSet.getString("akhir"),
                            resultSet.getString("uang_lembur"),
                            resultSet.getString("justifikasi")
                        )
                        lemburDataList.add(data)
                    }
                } finally {
                    connection.close()
                }
            }
        }
        return LemburList(lemburDataList)
    }

    private fun getDataDinas(username: String): DinasList {
        val dinasDataList = mutableListOf<DinasData>()
        GlobalScope.launch(Dispatchers.IO) {
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query = """
                        Select b.nomor,b.tanggal_pengajuan,b.kode_pekerja,a.nama,b.keterangan,
                        b.kendaraan,b.tujuan,b.mulai,b.akhir,b.data_upload FROM spd as b join biodata as a ON a.kode_pekerja=b.kode_pekerja 
                        JOIN master_unit as c ON a.cost_center_pengguna=c.cost_center_pengguna 
                        JOIN reviewer as d ON c.id_org_unit=d.id_org_unit where d.no_pers=?
                        AND b.status='Pending'""".trimIndent()
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, username)
                    val resultSet = statement.executeQuery()

                    while (resultSet.next()) {
                        val data = DinasData(
                            resultSet.getString("nomor"),
                            resultSet.getString("tanggal_pengajuan"),
                            resultSet.getString("kode_pekerja"),
                            resultSet.getString("nama"),
                            resultSet.getString("keterangan"),
                            resultSet.getString("kendaraan"),
                            resultSet.getString("asal"),
                            resultSet.getString("tujuan"),
                            resultSet.getString("mulai"),
                            resultSet.getString("akhir"),
                            resultSet.getString("data_upload")
                        )
                        dinasDataList.add(data)
                    }
                } finally {
                    connection.close()
                }
            }
        }
        return DinasList(dinasDataList)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        when (item.itemId) {
            R.id.menupanduan -> {
                val pdfFileName = "panduan_eworks.pdf"

                // Create a DownloadManager request
                val request =
                    DownloadManager.Request(Uri.parse("android.resource://${packageName}/raw/${pdfFileName}"))

                // Set the title for the notification (visible in the downloads UI)
                request.setTitle("Downloading Panduan eWorks PDF")

                // Set the description for the notification
                request.setDescription("Downloading...")

                // Set the local destination for the downloaded file
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    pdfFileName
                )

                // Get the DownloadManager service
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                // Enqueue the download request
                val downloadId = downloadManager.enqueue(request)
                Toast.makeText(this, "Downloading Panduan eWorks PDF...", Toast.LENGTH_SHORT).show()
            }

            R.id.menuHome -> {
                // Handle the "Home" item click (replace with your desired activity)
                val intent = Intent(this@ReviewActivity, ReviewActivity::class.java)
                val userBundle = Bundle()

                userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
                intent.putExtra("user_bundle", userBundle)
                startActivity(intent)
            }

            R.id.menulembur -> {
                Log.d("Test", "Clicked")
                // Handle the "Lembur" item click (replace with your desired activity)
                val intent = Intent(this@ReviewActivity, LemburActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
                intent.putExtra("user_bundle", userBundle)
                startActivity(intent)
            }

            R.id.menudinas -> {
                Log.d("Test", "Clicked")
                // Handle the "Lembur" item click (replace with your desired activity)
                val intent = Intent(this@ReviewActivity, DinasActivity::class.java)
                val userBundle = Bundle()
                userBundle.putParcelable("user", user) // Serialize the user object to a Bundle
                intent.putExtra("user_bundle", userBundle)
                startActivity(intent)
            }

            R.id.menulogout -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@ReviewActivity)
                builder.setMessage("Want to log out?")
                    .setNegativeButton("No", object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface, i: Int) {

                        }
                    })
                    .setPositiveButton("YES", object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface, i: Int) {
                            // Get an instance of your SharedPreferencesManager
                            val sharedPreferencesManager =
                                SharedPreferencesManager(this@ReviewActivity)

                            // Clear user data (and other data if needed)
                            sharedPreferencesManager.clearUserData()

                            // Navigate to the MainActivity or login screen
                            startActivity(Intent(this@ReviewActivity, MainActivity::class.java))
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
        } catch (e: NullPointerException) {
            // Handle the case where the bundle or user object is not found
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@ReviewActivity)
        builder.setMessage("Want to log out?")
            .setNegativeButton("No", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {

                }
            })
            .setPositiveButton("YES", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    startActivity(Intent(this@ReviewActivity, MainActivity::class.java))
                }
            })
            .show()
    }

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


    private fun setUsername(navigationView: NavigationView, username: String) {
        println("status: $username")
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.username_show)
        usernameTextView.text = username
    }

    private fun setDashboard(user: Reviewer) {
        val namaTV = binding.namaAkun
        val fungsiTV = binding.pekerjaan
        val rv = binding.recyclerView
        val bottomNavigation = binding.bottomNavigation
        namaTV.setText(user.nama)
        fungsiTV.setText(user.posisi)
        setUsername(navigationView, user.nama)
    }

    private fun getName(kode: String) {
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
                        Log.d("test", nama)
                        setUsername(navigationView, nama)
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@ReviewActivity,
                                "Failed Connect To Database",
                                Toast.LENGTH_SHORT
                            ).show()
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
                Toast.makeText(this@ReviewActivity, "Tidak dapat tersambung", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            loading!!.visibility = View.VISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            loading!!.visibility = View.INVISIBLE
        }
    }
}