package com.example.pertaminaapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.connection.lembur
import com.example.pertaminaapp.databinding.ActivityAtasanBinding
import com.example.pertaminaapp.databinding.ActivityPekerjaBinding
import com.example.pertaminaapp.session.SessionManager
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.w3c.dom.Text
import java.nio.charset.StandardCharsets
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.HashMap
import kotlin.random.Random

class PekerjaActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding : ActivityPekerjaBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var drawer : DrawerLayout
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView : NavigationView
    private lateinit var loading : LinearLayout
    private lateinit var kode : String
    private lateinit var mbunlde : Bundle
    private lateinit var setuju:TextView
    private lateinit var tolak : TextView
    private lateinit var revisi: TextView
    private lateinit var tunda : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPekerjaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.nav_view)
        setuju = binding.setuju
        tolak = binding.tolak
        revisi = binding.revisi
        tunda = binding.tertunda
        drawer =findViewById(R.id.drawer_layout)
        setSupportActionBar(toolbar);
        loading = findViewById(R.id.layout_loading)
        getBundle()
        navigationView.setNavigationItemSelectedListener(this)
        setDashboard(kode)
//        startRealTimeUpdates()
        getName(kode)
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
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menupanduan -> {
                // Handle the "Panduan" item click (replace with your desired activity)
                val pdfFileName = "panduan_eworks.pdf"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(
                    Uri.parse("android.resource://${packageName}/raw/${pdfFileName}"),
                    "application/pdf"
                )
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Handle the case where no activity is available to open the PDF
                    // You can display a message to the user or implement a PDF viewer
                }
            }
            R.id.menuHome -> {
                // Handle the "Home" item click (replace with your desired activity)
                val intent = Intent(this@PekerjaActivity, PekerjaActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString("kode", kode)
                intent.putExtra("user", mBundle)
                startActivity(intent)
            }
            R.id.menulembur -> {
                Log.d("Test","Clicked")
                // Handle the "Lembur" item click (replace with your desired activity)
                val intent = Intent(this@PekerjaActivity, LemburActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString("kode", kode)
                intent.putExtra("user", mBundle)
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
                            startActivity(Intent(this@PekerjaActivity, MainActivity::class.java))
                        }
                    })
                    .show()
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }
    private fun getBundle(){
        try{
            mbunlde = intent?.getBundleExtra("user")!!
            if(mbunlde != null){
                kode =mbunlde.getString("kode")!!
            }
        }catch(e: NullPointerException) {
            kode = "Guest"
        }
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

    private fun setUsername(navigationView: NavigationView, username: String) {
        println("status: $username")
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.username_show)
        usernameTextView.text = username
        runOnUiThread {
            setLoading(false)
        }
    }

    private fun setDashboard(kode: String){
        setLoading(true)
        GlobalScope.launch(Dispatchers.IO) {
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query = "SELECT * FROM spkl WHERE kode_pekerja = ?"
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, kode)
                    val resultSet: ResultSet = preparedStatement.executeQuery()
                    var approveCount = 0
                    var divertedCount = 0
                    var returnCount = 0
                    var rejectedCount = 0
                    var pendingCount = 0
                    var reviewCount = 0
                    while (resultSet.next()) {
                        // Process each row of data here
                        val status = resultSet.getString("status")
                        if(status == "Returned"){
                            returnCount++
                        }else if(status == "Diverted"){
                            divertedCount++
                        }else if(status == "Rejected"){
                            rejectedCount++
                        }else if(status == "Pending"){
                            pendingCount++
                        }else if(status == "Review"){
                            reviewCount++
                        }else{
                            approveCount++
                        }
                    }
                    // Update UI on the main thread
                    runOnUiThread {
                        setuju.text = approveCount.toString()
                        tolak.text = rejectedCount.toString()
                        revisi.text = returnCount.toString()
                        tunda.text = returnCount.toString()
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
                runOnUiThread {
                    Toast.makeText(this@PekerjaActivity,"Tidak dapat tersambung",Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                setLoading(false)
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