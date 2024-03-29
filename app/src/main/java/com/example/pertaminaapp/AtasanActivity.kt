package com.example.pertaminaapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.example.pertaminaapp.databinding.ActivityAtasanBinding
import com.example.pertaminaapp.session.SessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class AtasanActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding:ActivityAtasanBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var drawer : DrawerLayout
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var menu : ImageView
    private lateinit var kode:String
    private lateinit var mbunlde : Bundle
    private lateinit var loading : LinearLayout
    private lateinit var navigationView : NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAtasanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        toolbar = findViewById(R.id.toolbar)
        drawer =findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        setSupportActionBar(toolbar)
        loading = findViewById(R.id.layout_loading)
        getBundle()
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
        navigationView.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(this,drawer,toolbar,
            R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
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
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menupanduan){
// Replace "sample.pdf" with the actual PDF file name in res/raw
            val pdfFileName = "panduan_eworks_atasan.pdf"
            // Create an Intent to open the PDF
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
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@AtasanActivity)
        builder.setMessage("Want to log out?")
            .setNegativeButton("No", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {

                }
            })
            .setPositiveButton("YES", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    startActivity(Intent(this@AtasanActivity, MainActivity::class.java))
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

    private fun getName(kode:String){
        setLoading(true)
        GlobalScope.launch(Dispatchers.IO) {
            // Check the username and password in the database
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query = "SELECT * FROM manager WHERE no_pers = ?"
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, kode)
                    val resultSet: ResultSet = preparedStatement.executeQuery()
                    runOnUiThread {
                        setLoading(false)
                    }
                    if (resultSet.next()) {
                        val nama = resultSet.getString("nama")
                        setUsername(navigationView, nama)
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@AtasanActivity, "Failed Connect To Database", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@AtasanActivity,"Tidak dapat tersambung", Toast.LENGTH_SHORT).show()
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