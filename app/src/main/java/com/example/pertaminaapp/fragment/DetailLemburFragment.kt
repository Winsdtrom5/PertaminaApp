package com.example.pertaminaapp.fragment

import android.media.Image
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.pertaminaapp.R
import com.example.pertaminaapp.connection.eworks.getConnection
import com.example.pertaminaapp.model.HolidayList
import com.example.pertaminaapp.model.LemburData
import com.example.pertaminaapp.model.Reviewer
import com.example.pertaminaapp.model.User
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.PreparedStatement

class DetailLemburFragment : Fragment() {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val READ_WRITE_PERMISSION_CODE = 123
    private val CAMERA_PERMISSION_CODE = 101
    private val PDF_REQUEST_CODE = 123
    private lateinit var holidayList: HolidayList

    private lateinit var pdfView: WebView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_lembur, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Retrieve arguments
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments
        val lemburData = arguments?.getParcelable<LemburData>("lemburData")
        val user = arguments?.getParcelable<Reviewer>("user")
        // Check if lemburData is not null
        if (lemburData != null && user != null) {
            // Bind data to views
            view.findViewById<TextView>(R.id.Nomor).text = lemburData.nomor
            view.findViewById<TextView>(R.id.TanggalPengajuan).text = lemburData.tanggal_pengajuan
            view.findViewById<TextView>(R.id.Nama).text = lemburData.nama
            view.findViewById<TextView>(R.id.Posisi).text = lemburData.posisi
            view.findViewById<TextView>(R.id.Pekerjaan).text = lemburData.pekerjaan
            view.findViewById<TextView>(R.id.JamMulai).text = lemburData.mulai
            view.findViewById<TextView>(R.id.JamSelesai).text = lemburData.akhir
            view.findViewById<TextView>(R.id.Uanglembur).text = lemburData.uang_lembur
            retrieveFileFromFirebaseStorage(lemburData.bukti_lembur)
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
                                preparedStatementUpdate.setString(3, lemburData.nomor)
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
                                UPDATE spkl
                                SET status = ?, keterangan = ?
                                WHERE nomor = ?
                            """.trimIndent()

                                val preparedStatementUpdate: PreparedStatement = connection.prepareStatement(updateQuery)

                                // Assuming 'reviewer' is an object with a 'nama' property
                                preparedStatementUpdate.setString(1, selectedItem)
                                preparedStatementUpdate.setString(2, keteranganText.toString())
                                preparedStatementUpdate.setString(3, lemburData.nomor)
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
    private fun retrieveFileFromFirebaseStorage(fileName: String) {
        val fileReference: StorageReference = storageRef.child(fileName)

        fileReference.downloadUrl.addOnSuccessListener { uri ->
            val fileExtension = getFileExtension(fileName)
            if (isImageFile(fileExtension)) {
                // Load the image into the ImageView
                val imageView = view?.findViewById<ImageView>(R.id.BuktiLemburimage)
                imageView?.visibility = View.VISIBLE
                imageView?.setImageURI(uri)
            } else if (isPdfFile(fileExtension)) {
                // Load the PDF into the PDFView
                loadPdf(uri.toString())
            } else {
                // Handle unsupported file type
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Handle failure to retrieve the file
            Toast.makeText(context, "Failed to retrieve file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPdf(pdfUri: String) {
        val pdfView: PDFView? = view?.findViewById(R.id.BuktiLemburpdf)
        pdfView?.visibility = View.VISIBLE

        // Clear previous PDF, if any
        pdfView?.fromUri(Uri.parse(pdfUri))
            ?.pages(0) // You can set the specific pages to display
            ?.load()
    }


    // Function to get the file extension from the file name
    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.')
    }

    // Function to check if the file is an image based on its extension
    private fun isImageFile(extension: String): Boolean {
        return extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp")
    }

    // Function to check if the file is a PDF based on its extension
    private fun isPdfFile(extension: String): Boolean {
        return extension.lowercase() == "pdf"
    }
}
