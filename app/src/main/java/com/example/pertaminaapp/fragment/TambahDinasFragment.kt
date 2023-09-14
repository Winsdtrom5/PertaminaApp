package com.example.pertaminaapp.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest
import com.example.pertaminaapp.R
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.Calendar


class TambahDinasFragment : Fragment() {
    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
    }
    private val CAMERA_PERMISSION_CODE = 101
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tambah_dinas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read and parse kota.json and provinsi.json
        val kotaDataList = readAndParseKotaJson()
        val provinsiDataList = readAndParseProvinsiJson()

        // Combine and display the data
        val combinedDataList = combineAndFormatData(kotaDataList, provinsiDataList)

        // Create an ArrayAdapter with the combined data
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            combinedDataList
        )

        // Set the adapter to the AutoCompleteTextView
        val textviewasal = view.findViewById<AutoCompleteTextView>(R.id.acasal)
        val textviewtujuan = view.findViewById<AutoCompleteTextView>(R.id.actujuan)
        // Set the adapter to the AutoCompleteTextView
        textviewasal.setAdapter(adapter)
        textviewtujuan.setAdapter(adapter)

        val tilberangkat = view.findViewById<TextInputLayout>(R.id.TITanggalberangkat)
        val tilpulang = view.findViewById<TextInputLayout>(R.id.TITanggalpulang)
        val edberangkat = tilberangkat.editText
        val edpulang = tilpulang.editText
        edberangkat?.let { editText ->
            // Set up click listener for the end icon of Tanggal
            tilberangkat.setEndIconOnClickListener {
                // Show date picker dialog
                showDatePickerDialog(editText)
            }
        }// Set a threshold for auto-suggestions
        edpulang?.let { editText ->
            // Set up click listener for the end icon of Tanggal
            tilpulang.setEndIconOnClickListener {
                // Show date picker dialog
                showDatePickerDialog(editText)
            }
        }
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)

        uploadButton.setOnClickListener {
            // Show a dialog or options to select the source (gallery or camera)
            showFilePickerDialog()
        }
    }
    private fun showFilePickerDialog() {
        val items = arrayOf("Choose from Gallery", "Take a Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Select a File")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> checkPermissioncamera()
                }
            }
            .show()
    }

    private fun checkPermissioncamera(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, open the camera
            takePhoto()
        } else {
            // Permission is not granted, request it from the user
            requestCameraPermission()
        }
    }
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
        )
    }
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            // Handle the case where no camera app is available
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    // Handle the selected image from the gallery
                    val selectedImageUri = data?.data
                    // Process the selected image here
                }
                CAMERA_REQUEST_CODE -> {
                    // Handle the captured photo from the camera
                    val photo = data?.extras?.get("data") as Bitmap?
                    // Process the captured photo here
                }
            }
        }
    }
    private fun readAndParseKotaJson(): List<JSONObject> {
        val kotaDataList = mutableListOf<JSONObject>()
        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.kota)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charsets.UTF_8)

            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                kotaDataList.add(jsonObject)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return kotaDataList
    }

    private fun readAndParseProvinsiJson(): List<JSONObject> {
        val provinsiDataList = mutableListOf<JSONObject>()
        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.provinsi)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charsets.UTF_8)

            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                provinsiDataList.add(jsonObject)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return provinsiDataList
    }

    private fun combineAndFormatData(kotaList: List<JSONObject>, provinsiList: List<JSONObject>): List<String> {
        val combinedDataList = mutableListOf<String>()
        for (kotaObject in kotaList) {
            val kotaId = kotaObject.optInt("ID_Kota")
            for (provinsiObject in provinsiList) {
                val provinsiId = provinsiObject.optInt("Id")
                if (kotaId == provinsiId) {
                    val kotaName = kotaObject.optString("Nama_Daerah")
                    val provinsiName = provinsiObject.optString("Kota")
                    val combinedData = "$kotaName, $provinsiName"
                    combinedDataList.add(combinedData)
                }
            }
        }
        return combinedDataList
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
}

