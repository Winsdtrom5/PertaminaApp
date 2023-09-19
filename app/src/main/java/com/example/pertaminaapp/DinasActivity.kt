package com.example.pertaminaapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.databinding.ActivityDinasBinding
import com.example.pertaminaapp.fragment.DaftarDinasFragment
import com.example.pertaminaapp.fragment.TambahDinasFragment
import com.example.pertaminaapp.fragment.TambahLemburFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class DinasActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityDinasBinding
    private lateinit var drawer : DrawerLayout
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView : NavigationView
    private lateinit var loading : LinearLayout
    private lateinit var spinner : Spinner
    private lateinit var kode : String
    private lateinit var mbunlde : Bundle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDinasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        spinner = binding.dropdown
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.nav_view)
        drawer =findViewById(R.id.drawer_layout)
        setSupportActionBar(toolbar);
        loading = findViewById(R.id.layout_loading)
        getBundle()
        navigationView.setNavigationItemSelectedListener(this)
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
        val selectedItemPosition = 0 // Replace with your desired position
        spinner.setSelection(selectedItemPosition)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle item selection here and show the corresponding fragment
                // Inside your activity's code
                val bundle = Bundle()
                bundle.putString("kode", kode)
                when (position) {
                    0 -> {
                        showTambahDinasFragment(bundle)
                    }
                    1 -> {
                        showDaftarDinasFragment(bundle)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case when nothing is selected
            }
        }
    }

    override fun onBackPressed() {
        // Handle the "Home" item click (replace with your desired activity)
        val intent = Intent(this@DinasActivity, PekerjaActivity::class.java)
        val mBundle = Bundle()
        mBundle.putString("kode", kode)
        intent.putExtra("user", mBundle)
        startActivity(intent)
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
                val intent = Intent(this@DinasActivity, PekerjaActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString("kode", kode)
                intent.putExtra("user", mBundle)
                startActivity(intent)
            }
            R.id.menulembur -> {
                Log.d("Test","Clicked")
                // Handle the "Lembur" item click (replace with your desired activity)
                val intent = Intent(this@DinasActivity, LemburActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString("kode", kode)
                intent.putExtra("user", mBundle)
                startActivity(intent)
            }
            R.id.menudinas -> {
                Log.d("Test","Clicked")
                // Handle the "Lembur" item click (replace with your desired activity)
                val intent = Intent(this@DinasActivity, DinasActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString("kode", kode)
                intent.putExtra("user", mBundle)
                startActivity(intent)
            }
            R.id.menulogout -> {
                // Handle the "Log Out" item click (replace with your desired activity)
                val intent = Intent(this@DinasActivity
                    , MainActivity::class.java)
                startActivity(intent)
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

    private fun getName(kode:String){
        setLoading(true)
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
                            Toast.makeText(this@DinasActivity, "Failed Connect To Database", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@DinasActivity,"Tidak dapat tersambung", Toast.LENGTH_SHORT).show()
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

    private fun showTambahDinasFragment() {
        val existingFragment = supportFragmentManager.findFragmentById(R.id.Fv1)

        if (existingFragment != null) {
            // A fragment is already in the container; replace it
            val fragment = TambahDinasFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.Fv1, fragment)
            transaction.commit()
        } else {
            // No fragment in the container; add a new one
            val fragment = TambahDinasFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.Fv1, fragment)
            transaction.commit()
        }
    }
    private fun showTambahDinasFragment(bundle: Bundle) {
        val fragment = TambahDinasFragment()
        fragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.Fv1, fragment)
        transaction.addToBackStack(null) // Optional: Add the transaction to the back stack
        transaction.commit()
    }

    private fun showDaftarDinasFragment(bundle: Bundle) {
        val fragment = DaftarDinasFragment()
        fragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.Fv1, fragment)
        transaction.addToBackStack(null) // Optional: Add the transaction to the back stack
        transaction.commit()
    }
}