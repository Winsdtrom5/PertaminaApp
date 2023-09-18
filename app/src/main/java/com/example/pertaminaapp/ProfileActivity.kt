package com.example.pertaminaapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.databinding.ActivityProfileBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var kode : String
    private lateinit var mbunlde : Bundle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        setData(kode)
    }

    private fun setData(kodePekerja : String){
        GlobalScope.launch(Dispatchers.IO) {
            // Check the username and password in the database
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query = "SELECT * FROM biodata WHERE kode_pekerja = ?"
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, kodePekerja)
                    val resultSet: ResultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        val nama = resultSet.getString("nama")
                        val tanggal = resultSet.getString("tanggal_lahir")
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val birthDate = LocalDate.parse(tanggal, dateFormatter)
                        val currentDate = LocalDate.now()
                        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id-ID"))
                        val tgl_lahir = birthDate.format(formatter)
                        val ageformat = Period.between(birthDate, currentDate).years
                        val age = "$ageformat Tahun"
                        val jurusan = resultSet.getString("jurusan")
                        val gender = resultSet.getString("jenis_kelamin")
                        val masaKerja = resultSet.getString("masa_kerja")
                        val formattedMasaKerja = "$masaKerja Tahun"
                        val fungsi = resultSet.getString("fungsi")
                        val tamat = resultSet.getString("pendidikan")
                        val pendidikan = "$tamat $jurusan"
                        val pola = resultSet.getString("pola_kerja")
                        val pjp = resultSet.getString("PJP")
                        val klasifikasi = resultSet.getString("klasifikasi_name")
                        val kota = resultSet.getString("kota_kerja")
                        val lokasi = resultSet.getString("lokasi")
                        val jabatan = "$klasifikasi $lokasi $kota"
                        runOnUiThread {
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
                            namaTV.setText(nama)
                            pendidikanTV.setText(pendidikan)
                            lama.setText(formattedMasaKerja)
                            genderTV.setText(gender)
                            namaProfile.setText(nama)
                            birth.setText(tgl_lahir)
                            umur.setText(age)
                            fungsiTV.setText(fungsi)
                            klasifikasiTV.setText(klasifikasi)
                            polaTV.setText(pola)
                            pjpTV.setText(pjp)
                            kode.setText(kodePekerja)
                            pekerjaan.setText(jabatan)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed Connect To Database", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@ProfileActivity,"Tidak dapat tersambung", Toast.LENGTH_SHORT).show()
            }
        }
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
}