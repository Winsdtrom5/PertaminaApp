package com.example.pertaminaapp.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.pertaminaapp.R
import com.example.pertaminaapp.connection.eworks.getConnection
import com.example.pertaminaapp.model.DinasData
import com.example.pertaminaapp.model.HolidayList
import com.example.pertaminaapp.model.Reviewer
import com.example.pertaminaapp.model.User
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.PreparedStatement
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DetailDinasFragment : Fragment() {
    private val READ_WRITE_PERMISSION_CODE = 123
    private val CAMERA_PERMISSION_CODE = 101
    private val PDF_REQUEST_CODE = 123
    private lateinit var holidayList: HolidayList
    private lateinit var pdfView: WebView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_dinas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments
        val dinasData = arguments?.getParcelable<DinasData>("dinasData")
        val user = arguments?.getParcelable<Reviewer>("user")
        if (dinasData != null && user != null) {
            // Bind data to views
            view.findViewById<EditText>(R.id.edNomor).setText(dinasData.nomor)
            view.findViewById<EditText>(R.id.edTanggalPengajuan).setText(dinasData.tanggal_pengajuan)
            view.findViewById<EditText>(R.id.edNama).setText(dinasData.nama)
            view.findViewById<EditText>(R.id.edAsal).setText(dinasData.asal)
            view.findViewById<EditText>(R.id.edPekerjaan).setText(dinasData.tujuan)
            view.findViewById<EditText>(R.id.edTanggalBerangkat).setText(dinasData.mulai)
            view.findViewById<EditText>(R.id.edTanggalPulang).setText(dinasData.akhir)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val startDate = LocalDate.parse(dinasData.mulai, formatter)
            val endDate = LocalDate.parse(dinasData.akhir, formatter)
            val daysDifference = ChronoUnit.DAYS.between(startDate, endDate).toString()
            view.findViewById<EditText>(R.id.edJumlahHari).setText(daysDifference)
            view.findViewById<EditText>(R.id.edTransportasi).setText(dinasData.kendaraan)
            view.findViewById<WebView>(R.id.BuktiDinas).loadUrl(dinasData.data_upload)
            val balasan = view.findViewById<AutoCompleteTextView>(R.id.Balasan)
            val options = arrayOf("Dialihkan", "Ditolak", "Disetujui","Dikembalikan")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
            // Set the adapter to AutoCompleteTextView
            balasan.setAdapter(adapter)
            val keterangan = view.findViewById<TextInputLayout>(R.id.Keterangan)
            val keteranganText = keterangan.editText
            balasan.setOnItemClickListener { parent, view, position, id ->
                // Get the selected item
                val selectedItem = parent.getItemAtPosition(position).toString()

                // Perform actions based on the selected item
                when (selectedItem) {
                    "Dialihkan" -> {
                        view.findViewById<TextView>(R.id.keterangantxt).visibility = View.GONE
                        keterangan.visibility = View.GONE
                        keteranganText?.isFocusable = false
                    }

                    "Ditolak" -> {
                        view.findViewById<TextView>(R.id.keterangantxt).visibility = View.VISIBLE
                        keterangan.visibility = View.VISIBLE
                        keteranganText?.isFocusable = true
                    }

                    "Disetujui" -> {
                        view.findViewById<TextView>(R.id.keterangantxt).visibility = View.GONE
                        keterangan.visibility = View.GONE
                        keteranganText?.isFocusable = false
                    }

                    "Dikembalikan" -> {
                        view.findViewById<TextView>(R.id.keterangantxt).visibility = View.VISIBLE
                        keterangan.visibility = View.VISIBLE
                        keteranganText?.isFocusable = true
                    }
                    // Add more cases as needed
                }

                // Button click listener
                val buttonApply = view.findViewById<Button>(R.id.buttonApply)
                buttonApply.setOnClickListener {
                    if (selectedItem == "Dialihkan" || selectedItem == "Disetujui") {
                        GlobalScope.launch(Dispatchers.IO) {
                            val connection = getConnection()
                            if (connection != null) {
                                val updateQuery = """
                                UPDATE spkl
                                SET status = ?, approver = ?
                                WHERE nomor = ?
                            """.trimIndent()

                                val preparedStatementUpdate: PreparedStatement = connection.prepareStatement(updateQuery)

                                // Assuming 'reviewer' is an object with a 'nama' property
                                preparedStatementUpdate.setString(1, selectedItem)
                                preparedStatementUpdate.setString(2, user.nama)
                                preparedStatementUpdate.setString(3, dinasData.nomor)
                                val rowsAffected = preparedStatementUpdate.executeUpdate()
                                if (rowsAffected > 0) {
                                    Log.d("A","Complete")
                                } else {
                                    // Update failed
                                }

                                // Close the prepared statement
                                preparedStatementUpdate.close()
                            } else {
                                // Handle the case where connection is null
                            }
                        }
                    }else if (selectedItem == "Ditolak" || selectedItem == "Dikembalikan"){
                        GlobalScope.launch(Dispatchers.IO) {
                            val connection = getConnection()
                            if (connection != null) {
                                val updateQuery = """
                                UPDATE spd
                                SET status = ?, approval = ?
                                WHERE nomor = ?
                            """.trimIndent()

                                val preparedStatementUpdate: PreparedStatement = connection.prepareStatement(updateQuery)

                                // Assuming 'reviewer' is an object with a 'nama' property
                                preparedStatementUpdate.setString(1, selectedItem)
                                preparedStatementUpdate.setString(2, keteranganText.toString())
                                preparedStatementUpdate.setString(3, dinasData.nomor)
                                val rowsAffected = preparedStatementUpdate.executeUpdate()
                                if (rowsAffected > 0) {
                                    Log.d("A", "Complete")
                                    // Show a success message using Toast
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Berhasil Menyimpan Data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Log.d("A", "Update failed")
                                    // Show an error message using Toast
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Gagal Menyimpan Data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                // Close the prepared statement
                                preparedStatementUpdate.close()
                            } else {
                                // Handle the case where connection is null
                            }
                        }
                    }
                }
            }
        }
    }
}
