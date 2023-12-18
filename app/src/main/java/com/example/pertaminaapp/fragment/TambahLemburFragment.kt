package com.example.pertaminaapp.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.pertaminaapp.LemburActivity
import com.example.pertaminaapp.R
import com.example.pertaminaapp.connection.eworks.getConnection
import com.example.pertaminaapp.model.Holiday
import com.example.pertaminaapp.model.HolidayList
import com.example.pertaminaapp.model.User
import com.example.pertaminaapp.session.EmailSender
import com.example.pertaminaapp.session.FileUploadRequest
import com.example.pertaminaapp.session.FileUtil
import com.example.pertaminaapp.session.GoogleDriveHelper
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.sql.SQLException
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TambahLemburFragment : Fragment() {
    private lateinit var posisi: AutoCompleteTextView
    private lateinit var ketHari: String
    private var user: User? = null
    private lateinit var datePickerDialog: DatePickerDialog
    private var selectedYear: Int = 0
    private lateinit var previewImage: ImageView
    private var tempImageFile: File? = null
    private lateinit var alertDialog: AlertDialog
    private val FILE_PICKER_REQUEST = 123
    private val CAMERA_PERMISSION_REQUEST = 124
    private val FILE_ACCESS_PERMISSION_REQUEST = 125
    private val CAMERA_CAPTURE_REQUEST = 126
    private lateinit var submitButton: Button
    private lateinit var holidayList: HolidayList
    private lateinit var edpekerjaan: EditText
    private lateinit var edmasuk: EditText  // Define edmasuk as a property
    private lateinit var edkeluar: EditText
    private lateinit var edtanggal: EditText
    private var isNormalPola: Boolean = false
    private var generatedId: Int = -1
    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>
    private lateinit var accessToken: String
    private lateinit var jsonKeyInputStream: InputStream
    private lateinit var filename :String
    private lateinit var filepath : String
    private var fileUri: Uri? = null
//    private var CLIENT_ID = "9b9f8467-419a-4dbc-9c62-2b6eb1a959f2"
//    private var CLIENT_SECRET = "JIB8Q~hN2E1QOgC3uvm06_krhlO0dcZiWxAYRbrT"
//    private var TENANT_ID = "89870a59-1408-40ee-a72a-22c7020ac747"
    private val CLIENT_ID = "872075133408-j3n3a0c2pjmidakis1rggu41i3cqb83e.apps.googleusercontent.com"
    private val CLIENT_SECRET = "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"
    private val REDIRECT_URI = "http://localhost"
    private val SCOPES = "https://www.googleapis.com/auth/drive" // Add more scopes if needed
    private val AUTHORIZATION_URL = "https://accounts.google.com/o/oauth2/auth"
    private val TOKEN_URL = "https://oauth2.googleapis.com/token"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_lembur, container, false)
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext())
        }
        val tilTanggal = view.findViewById<TextInputLayout>(R.id.TITanggal)
        edtanggal = tilTanggal.editText!!
        val tilMasuk = view.findViewById<TextInputLayout>(R.id.TIMasuk)
        edmasuk = tilMasuk.editText!!
        val tilKeluar = view.findViewById<TextInputLayout>(R.id.TIKeluar)
        edkeluar = tilKeluar.editText!!
        val tilpekerjaan = view.findViewById<TextInputLayout>(R.id.pekerjaan)
        edpekerjaan = tilpekerjaan.editText!!
        edkeluar.isEnabled = false
        val tilnama = view.findViewById<TextInputLayout>(R.id.nama)
        val ednama = tilnama.editText
        val tilShift = view.findViewById<TextInputLayout>(R.id.pola)
        val edshift = tilShift.editText
        submitButton = view.findViewById(R.id.submitButton)
//        previewImage = view.findViewById(R.id.previewImage1)
        user = arguments?.getParcelable("user")
        holidayList = arguments?.getParcelable("holidayList") ?: HolidayList(emptyList())
        user?.let { getData(it) }
        val nama = user?.nama
        val shift = user?.pola
        if (edshift != null && ednama != null) {
            edshift.setText(shift)
            edshift.isEnabled = false
            ednama.setText(nama)
            ednama.isEnabled = false
        }
        val jsonResourceId = R.raw.pertaminaapp
        val jsonKey = resources.openRawResource(R.raw.pertaminaapp)
        FirebaseApp.initializeApp(requireContext())
        edtanggal.let { editText ->
            tilTanggal.setEndIconOnClickListener {
                showDatePickerDialog(editText)
            }
        }
        edmasuk.let { editText ->
            tilMasuk.setEndIconOnClickListener {
                showTimePickerDialog(edmasuk)
            }
        }

        edkeluar.let { editText ->
            tilKeluar.setEndIconOnClickListener {
               if (!edmasuk.text.isNullOrBlank()) {
                    showTimePickerDialog(edkeluar)
                }
            }
        }
        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val isFormValid = isFormValid()
//                        && isImageUploaded()
                submitButton.isEnabled = isFormValid
            }
        })
        view.findViewById<View>(R.id.uploadfile).setOnClickListener {
            showImagePickerDialog()
        }
        submitButton.setOnClickListener {
            val loadingLayout = (activity as LemburActivity).findViewById<LinearLayout>(R.id.layout_loading)
            val Text = loadingLayout.findViewById<TextView>(R.id.txt)
            loadingLayout.visibility= View.VISIBLE
            Text.setText("Memvalidasi Form")
            if (isFormValid()) {
                Text.setText("Mengecek Data")
                user?.kode?.let { it1 ->
                    cekLembur(
                        it1,
                        edtanggal.toString(),
                        edmasuk.toString(),
                        edkeluar.toString()
                    ) { isRecordExists ->
                        if (isRecordExists) {
                            Toast.makeText(requireContext(), "Data Sudah Ada", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            // Create an email sender object and call the sendEmail function
                            generateId()
                            val emailSender = EmailSender()
                            // Send the email
                            val recipientEmail = user?.email
                            val subject = "Permintaan Lembur Nomor $generatedId"
                            val message = """
                                    Halo,
                                
                                    Saya, $nama, ingin mengajukan permintaan lembur dengan rincian sebagai berikut:
                                
                                    - Tanggal: ${edtanggal.text}
                                    - Waktu Mulai: ${edmasuk.text}
                                    - Waktu Selesai: ${edkeluar.text}
                                    - Divisi: ${user?.klasifikasi}
                                    - Detail Pekerjaan: ${edpekerjaan.text}
                                
                                    Terima kasih.
                                    
                                    Salam,
                                    [$nama]
                                """
                            if (recipientEmail != null) {
                                fileUri?.let { it2 ->
//                                    uploadFileToFirebaseStorage(it2) { filePath ->
//                                        // Call insertLemburData with the file path
//
//                                    }
                                    activity?.runOnUiThread {
                                        Text.setText("Menginput Data")
                                    }
//                                    if (filePath != null) {
//
//                                    }else{
//                                        activity?.runOnUiThread {
//                                            Text.setText("Gagal Menginput Data")
//                                        }
//                                        activity?.runOnUiThread {
//                                            loadingLayout.visibility=View.INVISIBLE
//                                        }
//                                    }
//                                    fileUri?.let { uri ->
//                                        val inputStream = requireContext().contentResolver.openInputStream(uri)
//
//                                        if (inputStream != null) {
//                                            val file = createTempFileFromInputStream(inputStream)
//
//                                            val accessToken = requestAccessToken(CLIENT_ID, CLIENT_SECRET)
//
//                                            if (accessToken != null) {
//                                                uploadFileToGoogleDrive(file) { link ->
//                                                    if (link != null) {
//                                                        Log.d("bruh", link)
                                                        insertLemburData(filepath)
                                                        activity?.runOnUiThread {
                                                            Text.setText("Mengirim Email")
                                                        }
                                                        emailSender.sendEmail(recipientEmail, subject, message)
                                                        activity?.runOnUiThread {
                                                            loadingLayout.visibility = View.INVISIBLE
                                                        }
                                                        activity?.runOnUiThread {
                                                            Text.setText("Mengirim Email")
                                                        }
                                                        emailSender.sendEmail(recipientEmail, subject, message)
                                                        activity?.runOnUiThread {
                                                            loadingLayout.visibility = View.INVISIBLE
                                                        }
//                                                    } else {
//                                                        Log.d("bruh", "Failed")
//                                                        activity?.runOnUiThread {
//                                                            Text.setText("Gagal Menginput Data")
//                                                        }
//                                                        activity?.runOnUiThread {
//                                                            loadingLayout.visibility = View.INVISIBLE
//                                                        }
//                                                    }
//                                                    // Perform additional actions here
//                                                }
//                                            } else {
//                                                Log.e("GoogleDrive", "Failed to get access token.")
//                                            }
//                                        } else {
//                                            Log.e("GoogleDrive", "InputStream is null")
//                                        }
//                                    }
//                                    fileUri?.let { uri ->
//                                        val inputStream = requireContext().contentResolver.openInputStream(uri)
//
//                                        if (inputStream != null) {
//                                            // Create a folder with the app's name in external storage app-specific directory
//                                            val folderName = "Pertamina"
//                                            val folder = File(requireContext().getExternalFilesDir(null), folderName)
//
//                                            if (!folder.exists()) {
//                                                if (folder.mkdirs()) {
//                                                    Log.d("bruh", "Folder created at: ${folder.absolutePath}")
//                                                } else {
//                                                    Log.e("bruh", "Failed to create folder at: ${folder.absolutePath}")
//                                                }
//                                            } else {
//                                                Log.d("bruh", "Folder already exists at: ${folder.absolutePath}")
//                                            }
//
//                                            // If you have a nested folder structure, create the nested folders
//                                            val nestedFolderName = "Octaviani Fangohoi/Lembur"
//                                            val nestedFolder = File(folder, nestedFolderName)
//
//                                            if (!nestedFolder.exists()) {
//                                                if (nestedFolder.mkdirs()) {
//                                                    Log.d("bruh", "Nested folder created at: ${nestedFolder.absolutePath}")
//                                                } else {
//                                                    Log.e("bruh", "Failed to create nested folder at: ${nestedFolder.absolutePath}")
//                                                }
//                                            } else {
//                                                Log.d("bruh", "Nested folder already exists at: ${nestedFolder.absolutePath}")
//                                            }
//
//                                            // Create a file in the specified nested folder
//                                            val file = File(nestedFolder, filename)
//
//                                            // Copy the content of the input stream to the file
//                                            inputStream.use { input ->
//                                                FileOutputStream(file).use { output ->
//                                                    input.copyTo(output)
//                                                }
//                                            }
//
//                                            // Now, 'file' contains the path to the saved file
//                                            val filePath = file.absolutePath
//
//                                            // Perform actions with the file path
//                                            Log.d("bruh", "File saved at: $filePath")
//
//                                            // Continue with your logic...
//                                        } else {
//                                            Log.e("GoogleDrive", "InputStream is null")
//                                        }
//                                    }


// After the user authenticates and receives the authorization code, call this function

//                                    if (filePath != null) {
//
//
                                }
                            }
                        }
                    }
                }

            } else {
                // Handle invalid form data
                Toast.makeText(
                    requireContext(),
                    "Please fill out the form completely.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return view
    }
    private fun createTempFileInFolder(folder: File, fileExtension: String): File {
        return File.createTempFile("temp_file", fileExtension, folder)
    }
    private fun initiateGoogleDriveAuthentication() {
        val authorizationUrl = "$AUTHORIZATION_URL?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&response_type=code&scope=$SCOPES"
        GetAuthorizationCodeTask().execute(authorizationUrl)
    }

    // AsyncTask to perform network operation in the background
    private class GetAuthorizationCodeTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String? {
            val authorizationUrl = params[0]

            return try {
                val url = URL(authorizationUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    readResponse(connection.inputStream)
                } else {
                    Log.e("GoogleDrive", "Failed to get authorization code. Response code: $responseCode")
                    null
                }
            } catch (e: Exception) {
                Log.e("GoogleDrive", "Error getting authorization code: ${e.message}")
                null
            }
        }

        override fun onPostExecute(result: String?) {
            // Handle the obtained authorization code here
            if (result != null) {
                Log.d("GoogleDrive", "Authorization Code: $result")
                // Now you can proceed with getting the access token
                (result)
            } else {
                // Failed to get authorization code
                Log.e("GoogleDrive", "Failed to get authorization code.")
            }
        }

        private fun readResponse(inputStream: InputStream): String {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            reader.close()

            return response.toString()
        }
    }

    private fun requestAccessToken(clientId: String, clientSecret: String): String? {
        val authorizationUrl = "$AUTHORIZATION_URL?client_id=$clientId&redirect_uri=$REDIRECT_URI&response_type=code&scope=$SCOPES"
        val authorizationCode = GetAuthorizationCodeTask().execute(authorizationUrl).get()

        // Check if the authorization code is obtained successfully
        if (authorizationCode != null) {
            // Now use the obtained authorization code to get the access token
            return requestAccessToken(authorizationCode, clientId, clientSecret)
        } else {
            Log.e("GoogleDrive", "Failed to obtain authorization code.")
            return null
        }
    }

    private fun requestAccessToken(authorizationCode: String, clientId: String, clientSecret: String): String? {
        try {
            val url = URL(TOKEN_URL)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true

            val requestBody = "code=$authorizationCode&client_id=$clientId&client_secret=$clientSecret&redirect_uri=$REDIRECT_URI&grant_type=authorization_code"
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(requestBody.toByteArray())
            outputStream.flush()

            val responseCode = connection.responseCode
            Log.d("GoogleDrive", "Access token request response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()

                val jsonResponse = JSONObject(response.toString())
                return jsonResponse.optString("access_token")
            } else {
                Log.e("GoogleDrive", "Failed to get access token. Response code: $responseCode")

                // Print the error response, if available
                try {
                    val errorStream = connection.errorStream
                    if (errorStream != null) {
                        val errorReader = BufferedReader(InputStreamReader(errorStream))
                        val errorResponse = StringBuilder()

                        var errorLine: String?
                        while (errorReader.readLine().also { errorLine = it } != null) {
                            errorResponse.append(errorLine)
                        }

                        errorReader.close()
                        Log.e("GoogleDrive", "Error response: $errorResponse")
                    }
                } catch (e: Exception) {
                    Log.e("GoogleDrive", "Error reading error response: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDrive", "Error getting access token: ${e.message}")
        }

        return null
    }

//
//    private suspend fun signInWithJsonKey(context: Context, jsonResourceId: Int): GoogleSignInAccount? {
//        // Load JSON key from resources
//        val jsonKey = context.resources.openRawResource(jsonResourceId)
//
//        // Configure GoogleSignInOptions
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
//            // Add any other options you need
//            .build()
//
//        // Create GoogleSignInClient with the configured options
//        val googleSignInClient = GoogleSignIn.getClient(context, gso)
//
//        return try {
//            // Attempt silent sign-in
//            val account = withContext(Dispatchers.IO) {
//                Tasks.await(googleSignInClient.silentSignIn())
//            }
//            account
//        } catch (e: ApiException) {
//            // Handle the exception, e.g., user is not signed in or sign-in failed
//            null
//        }
//    }
//
//    private fun uploadFileToFirebaseStorage(fileUri: Uri, callback: (String?) -> Unit) {
//        FirebaseApp.initializeApp(requireContext());
//        val storage = Firebase.storage
//        val imagesRef = storage.reference.child("images")
//        val fileReference = imagesRef.child(filename)
//
//        val uploadTask = fileReference.putFile(fileUri)
//
//        uploadTask.addOnSuccessListener { taskSnapshot ->
//            // File uploaded successfully
//            val downloadUrl = taskSnapshot.storage.downloadUrl
//            // Now you can save the downloadUrl to Firestore or perform other actions
//            // For example, update the UI to show the file details
//            callback(filename)
//        }.addOnFailureListener { exception ->
//            // Handle unsuccessful uploads
//            // You might want to show an error message to the user
//            callback(null)
//        }
//    }


    private fun cekLembur(
        kode_pekerja: String,
        tanggal: String,
        mulai: String,
        akhir: String,
        callback: (Boolean) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val connection = getConnection()
            if (connection != null) {
                try {
                    val sql =
                        "SELECT COUNT(*) FROM spkl WHERE kode_pekerja = ? AND tanggal = ? AND mulai = ? AND akhir = ?"
                    val preparedStatement = connection.prepareStatement(sql)

                    preparedStatement.setString(1, kode_pekerja)
                    preparedStatement.setString(2, tanggal)
                    preparedStatement.setString(3, mulai)
                    preparedStatement.setString(4, akhir)

                    val resultSet = preparedStatement.executeQuery()

                    val isRecordExists = resultSet.next() && resultSet.getInt(1) > 0
                    callback(isRecordExists)
                } catch (e: SQLException) {
                    e.printStackTrace()
                    callback(false)
                    Log.d("Ambasing","Amkaming")
                } finally {
                    connection.close()
                }
            } else {
                Log.d("MyApp","Ambasing")
            }
        }
    }


    private fun insertLemburData(filePath: String) {
        GlobalScope.launch {
            val connection = getConnection()
            if (connection != null) {
                try {
                    Log.d("MyApp", "Inside insertLemburData function")
                    val upahlembur = user?.upah?.toDoubleOrNull()?.div(173)
                    val satujam = "01:00"
                    val subjam = satujam.substring(0, satujam.length - 3).toDouble()
                    val submenit = satujam.substring(satujam.length - 2).toDouble()
                    val jammenit = (subjam.toInt() * 60).toDouble()
                    val edmasukText = edmasuk.text.toString()
                    val edkeluarText = edkeluar.text.toString()// Get the formatted time as a string
                    val lamaLembur = calculateTimeDifference(
                        edmasukText,
                        edkeluarText
                    ) // Assign the appropriate value for keterangan_hari

                    var biasa1: Double = 0.0
                    var biasa2: Double = 0.0
                    var libur1_8: Double = 0.0
                    var libur9: Double = 0.0
                    var libur10: Double = 0.0
                    var uangLembur: Double = 0.0
                    var lamaLemburKonversi: Double = 0.0
                    if (lamaLembur >= 1) {
                        biasa1 = ((jammenit + submenit.toDouble()) * 1.5) / 60
                        biasa2 =
                            if (lamaLembur >= jammenit + submenit) (lamaLembur - jammenit - submenit) * 2.0 else 0.0
                    } else {
                        biasa1 = (lamaLembur * 1.5)
                        biasa2 = 0.0
                    }

                    if (lamaLembur >= 8) {
                        libur1_8 = 8.0 * 2.0
                    } else {
                        libur1_8 = lamaLembur * 2.0
                    }

                    if (lamaLembur >= 9) {
                        libur9 = ((jammenit + submenit) * 3.0) / 60
                    } else if (lamaLembur > 8 && lamaLembur < 9) {
                        libur9 = (lamaLembur - 8) * 3.0
                    } else {
                        libur9 = 0.0
                    }

                    if (lamaLembur >= 10 || lamaLembur > 9) {
                        libur10 = (lamaLembur - 9) * 4.0
                    } else {
                        libur10 = 0.0
                    }

                    if (ketHari == "Biasa") {
                        uangLembur = (biasa1 + biasa2) * upahlembur!!
                        lamaLemburKonversi = biasa1 + biasa2
                    } else if (ketHari == "Libur" && lamaLembur > 0 && lamaLembur <= 8) {
                        uangLembur = (libur1_8 * 2) * upahlembur!!
                        lamaLemburKonversi = libur1_8 * 2
                    } else if (ketHari == "Libur" && lamaLembur > 0 && lamaLembur > 8 && lamaLembur <= 9) {
                        uangLembur = (libur1_8 + libur9) * upahlembur!!
                        lamaLemburKonversi = libur1_8 + libur9
                    } else if (ketHari == "Libur" && (lamaLembur >= 10 || lamaLembur > 9)) {
                        uangLembur = (libur1_8 + libur9 + libur10) * upahlembur!!
                        lamaLemburKonversi = libur1_8 + libur9 + libur10
                    }
                    val sql = "INSERT INTO spkl (tanggal_pengajuan, kode_pekerja, nama, pekerjaan, tanggal, ket_hari, posisi, mulai, akhir, lama_lembur_konversi, lama_lembur, uang_lembur, status, keterangan, Approver, Justifikasi) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    val preparedStatement = connection.prepareStatement(sql)
                    Log.d("MyApp", "SQL Statement: $sql")
                    preparedStatement.setString(1, getTodayDateWithTime())
                    preparedStatement.setString(2, user?.kode)
                    preparedStatement.setString(3, user?.nama)
                    val formatter = SimpleDateFormat("yyyy-MM-dd")
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val date = formatter.parse(edtanggal.toString())
                    val timemulai = timeFormat.parse(edmasuk.toString())
                    val timepulang = timeFormat.parse(edkeluar.toString())
                    preparedStatement.setString(4, edpekerjaan.toString()) // Bind the value for 'pekerjaan'
                    preparedStatement.setDate(5, date as java.sql.Date?)
                    preparedStatement.setString(6, ketHari) // Bind the value for 'ket_hari'
                    preparedStatement.setString(7, "") // Bind the value for 'posisi'
                    preparedStatement.setTime(8, Time(timemulai.time))
                    preparedStatement.setTime(9, Time(timepulang.time))
                    preparedStatement.setDouble(10, lamaLemburKonversi)
                    preparedStatement.setDouble(11, lamaLembur.toDouble())
                    preparedStatement.setInt(12, uangLembur.toInt())
                    preparedStatement.setString(13, "") // Bind the value for 'status'
                    preparedStatement.setString(14, "") // Bind the value for 'keterangan'
                    preparedStatement.setString(15, "") // Bind the value for 'Approver'
                    preparedStatement.setString(16, filePath) // Bind the value for 'Justifikasi'
                    val affectedRows = preparedStatement.executeUpdate()

                    if (affectedRows == 1) {
                        // Get the generated keys (including the ID)
                        val generatedKeys = preparedStatement.generatedKeys
                        if (generatedKeys.next()) {
                            generatedId = generatedKeys.getInt(1) // Get the ID
                            Log.d("MyApp", "Generated ID: $generatedId")
                        }
                    }
                } catch (e: SQLException) {
                    Log.e("MyApp", "Error in insertLemburData: ${e.message}", e)
                    e.printStackTrace()
                } finally {
                    connection.close()
                }
            }else{
                Log.d("MyApp2","Ambasing")
            }
        }
    }
    private fun generateId(){
        GlobalScope.launch(Dispatchers.IO) {
            val connection = getConnection()
            if (connection != null) {
                try {
                    val maxNomorSql = "SELECT MAX(nomor) FROM spkl"
                    val maxNomorStatement = connection.prepareStatement(maxNomorSql)
                    val maxNomorResultSet = maxNomorStatement.executeQuery()

                    // Check if the result set has a value
                    val maxNomor = if (maxNomorResultSet.next()) maxNomorResultSet.getInt(1) else 0
                    generatedId = maxNomor + 1
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.d("Ambasing","Amkaming")
                } finally {
                    connection.close()
                }
            } else {
                Log.d("MyApp","Ambasing")
            }
        }
    }
    private fun calculateTimeDifference(startTime: String, endTime: String): Int {
        val dateFormat = SimpleDateFormat("HH:mm")
        val startDate = dateFormat.parse(startTime)
        val endDate = dateFormat.parse(endTime)
        val diff = (endDate.time - startDate.time) / (1000 * 60) // Convert milliseconds to minutes
        return diff.toInt()
    }

    fun getTodayDateWithTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun showTimePickerDialog(EditText: EditText) {
        val calendar = Calendar.getInstance()
        val isNormalPola = user?.pola == "Normal"
        val initialHour = if (isNormalPola) 18 else calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val selectedTime = "$selectedHour:$selectedMinute"

                if (isValidTime(selectedTime)) {
                    // Format the time to 12-hour format
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    EditText.setText(formattedTime)
                } else {
                    Toast.makeText(requireContext(), "Invalid time selected", Toast.LENGTH_SHORT)
                        .show()
                    showTimePickerDialog(EditText)
                }
            },
            initialHour,
            initialMinute,
            true
        )

        val timePickerField = timePickerDialog.javaClass.getDeclaredField("mTimePicker")
        timePickerField.isAccessible = true
        val timePickerView = timePickerField.get(timePickerDialog) as TimePicker

        timePickerView.setIs24HourView(true)  // Set time picker to 24-hour format

        timePickerDialog.show()
    }

    private fun isValidTime(time: String): Boolean {
        val parts = time.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            // Check if the time is greater than 18:00 or less than 06:00 (next day)
            return (hour >= 18) || (hour < 6) || (hour == 6 && minute == 0)
        }
        return false
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // Set accepted MIME types
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/jpg", "image/png", "application/pdf"))

        startActivityForResult(intent, FILE_PICKER_REQUEST)
    }
//
//    private fun getSiteId(accessToken: String, callback: (String?) -> Unit) {
//        val url = "https://graph.microsoft.com/v1.0/sites/root"
//        val headers = mapOf("Authorization" to "Bearer $accessToken")
//
//        val jsonObjectRequest = object : JsonObjectRequest(
//            Request.Method.GET, url, null,
//            Response.Listener<JSONObject> { response ->
//                val siteId = response.optString("id", null)
//                callback(siteId)
//            },
//            Response.ErrorListener { error ->
//                Log.e("UploadError", "Error getting Site ID: ${error.message}")
//                callback(null)
//            }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                return headers.toMutableMap()
//            }
//        }
//        val requestQueue = Volley.newRequestQueue(requireContext())
//        // Add the request to the queue
//        requestQueue.add(jsonObjectRequest)
//    }
//
//    private fun getDocumentLibraryId(siteId: String, accessToken: String, callback: (String?) -> Unit) {
//        val url = "https://graph.microsoft.com/v1.0/sites/$siteId/drives/root"
//        val headers = mapOf("Authorization" to "Bearer $accessToken")
//
//        val jsonObjectRequest = object : JsonObjectRequest(
//            Request.Method.GET, url, null,
//            Response.Listener<JSONObject> { response ->
//                val documentLibraryId = response.optString("id", null)
//                callback(documentLibraryId)
//            },
//            Response.ErrorListener { error ->
//                Log.e("UploadError", "Error getting Document Library ID: ${error.message}")
//                callback(null)
//            }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                return headers.toMutableMap()
//            }
//        }
//        val requestQueue = Volley.newRequestQueue(requireContext())
//        // Add the request to the queue
//        requestQueue.add(jsonObjectRequest)
//    }
//    private fun getAccessToken(clientId: String, clientSecret: String, tenantId: String): String? {
//        try {
//            val tokenUrl = "https://login.microsoftonline.com/$tenantId/oauth2/v2.0/token"
//            val connection = URL(tokenUrl).openConnection() as HttpURLConnection
//            connection.requestMethod = "POST"
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
//            connection.doOutput = true
//
//            val requestBody = "client_id=$clientId&scope=https://graph.microsoft.com/.default&client_secret=$clientSecret&grant_type=client_credentials"
//            val outputStream = DataOutputStream(connection.outputStream)
//            outputStream.writeBytes(requestBody)
//            outputStream.flush()
//            outputStream.close()
//
//            val responseCode = connection.responseCode
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
//                val jsonObject = JSONObject(jsonResponse)
//                Log.e("Uploaderror", "accesstokensuccesss")
//                return jsonObject.getString("access_token")
//            } else {
//                // Print the error stream for additional details
//                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
//                Log.e("Uploaderror", "accesstokenfailed: $errorResponse")
//            }
//        } catch (e: Exception) {
//            Log.e("Uploaderror", "Exception during access token retrieval: ${e.message}")
//            e.printStackTrace()
//        }
//
//        return null
//    }
//
//    private fun uploadFileToOneDrive(callback: (String?) -> Unit) {
//        try {
//            Log.d("UploadError", "Trying to upload file. File URI: $fileUri")
//            val accessToken = getAccessToken(CLIENT_ID, CLIENT_SECRET, TENANT_ID)
//            Log.d("UploadError", "Access Token: $accessToken")
//            if (accessToken != null) {
//                getSiteId(accessToken) { siteId ->
//                    if (siteId != null) {
//                        getDocumentLibraryId(siteId, accessToken) { documentLibraryId ->
//                            if (documentLibraryId != null) {
//                                fileUri?.let { uri ->
//                                    val contentResolver = requireContext().contentResolver
//                                    val inputStream = contentResolver.openInputStream(uri)
//                                    if (inputStream != null) {
//                                        try {
//                                            val tempFile = createTempFileFromInputStream(inputStream)
//                                            Log.d("UploadError", "Temp file created: ${tempFile.absolutePath}")
//
//                                            // Use the correct endpoint for uploading to OneDrive
//                                            val uploadUrl = "https://graph.microsoft.com/v1.0/drives/$documentLibraryId/items/root:/Test/${tempFile.name}:/content"
//
//                                            val requestQueue = Volley.newRequestQueue(requireContext())
//
//                                            val volleyFileUploadRequest = FileUploadRequest(
//                                                Request.Method.PUT,
//                                                uploadUrl,
//                                                tempFile,
//                                                accessToken,
//                                                { link ->
//                                                    // File uploaded successfully, 'link' contains the webUrl
//                                                    // Use the link or perform further actions
//                                                    Log.d("UploadError", "File uploaded successfully. Link: $link")
//                                                    callback(link)
//                                                },
//                                                { error ->
//                                                    // Handle error
//                                                    Log.e("UploadError", "Error uploading file: ${error.message}")
//                                                    if (error.networkResponse != null) {
//                                                        Log.e("UploadError", "Response code: ${error.networkResponse.statusCode}")
//                                                        Log.e("UploadError", "Response data: ${String(error.networkResponse.data)}")
//                                                    }
//                                                    callback(null) // Pass null link to indicate failure
//                                                }
//                                            )
//
//                                            // Add the request to the queue
//                                            requestQueue.add(volleyFileUploadRequest)
//                                        } catch (e: Exception) {
//                                            Log.e("UploadError", "Exception creating temp file: ${e.message}")
//                                            callback(null) // Pass null link to indicate failure
//                                        } finally {
//                                            inputStream.close()
//                                        }
//                                    } else {
//                                        // Handle case where inputStream is null
//                                        Log.e("UploadError", "InputStream is null")
//                                        callback(null) // Pass null link to indicate failure
//                                    }
//                                }
//                            } else {
//                                Log.e("UploadError", "Error obtaining Document Library ID.")
//                                callback(null)
//                            }
//                        }
//                    } else {
//                        Log.e("UploadError", "Error obtaining Site ID.")
//                        callback(null)
//                    }
//                }
//            } else {
//                Log.e("UploadError", "Error obtaining access token.")
//                callback(null)
//            }
//        } catch (e: Exception) {
//            Log.e("UploadError", "Exception during file upload: ${e.message}")
//            e.printStackTrace()
//            callback(null) // Pass null link to indicate failure
//        }
//    }

//    private fun getFolderId(folderUrl: String): String? {
//        try {
//            val regex = Regex("/folders/([a-zA-Z0-9_-]+)")
//            val matchResult = regex.find(folderUrl)
//            return matchResult?.groupValues?.getOrNull(1)
//        } catch (e: Exception) {
//            Log.e("bruh", "Exception during folder ID retrieval: ${e.message}")
//            e.printStackTrace()
//            return null
//        }
//    }
//
//    private fun requestDeviceCode(clientId: String, scope: String): JSONObject? {
//        try {
//            val url = URL(DEVICE_CODE_URL)
//            val connection = url.openConnection() as HttpURLConnection
//
//            connection.requestMethod = "POST"
//            connection.doOutput = true
//
//            val requestBody = "client_id=$clientId&scope=$scope"
//            val outputStream: OutputStream = connection.outputStream
//            outputStream.write(requestBody.toByteArray())
//            outputStream.flush()
//
//            val responseCode = connection.responseCode
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                val reader = BufferedReader(InputStreamReader(connection.inputStream))
//                val response = StringBuilder()
//
//                var line: String?
//                while (reader.readLine().also { line = it } != null) {
//                    response.append(line)
//                }
//
//                reader.close()
//
//                return JSONObject(response.toString())
//            } else {
//                Log.e("GoogleDrive", "Failed to request device code. Response code: $responseCode")
//            }
//        } catch (e: Exception) {
//            Log.e("GoogleDrive", "Error requesting device code: ${e.message}")
//        }
//
//        return null
//    }
//
//    // Step 2: Poll for access token
//    private fun pollForAccessToken(clientId: String, clientSecret: String, deviceCode: String): JSONObject? {
//        try {
//            val url = URL(TOKEN_URL)
//            val connection = url.openConnection() as HttpURLConnection
//
//            connection.requestMethod = "POST"
//            connection.doOutput = true
//
//            val requestBody = "client_id=$clientId&client_secret=$clientSecret&device_code=$deviceCode&grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code"
//            val outputStream: OutputStream = connection.outputStream
//            outputStream.write(requestBody.toByteArray())
//            outputStream.flush()
//
//            val responseCode = connection.responseCode
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                val reader = BufferedReader(InputStreamReader(connection.inputStream))
//                val response = StringBuilder()
//
//                var line: String?
//                while (reader.readLine().also { line = it } != null) {
//                    response.append(line)
//                }
//
//                reader.close()
//
//                return JSONObject(response.toString())
//            } else {
//                Log.e("GoogleDrive", "Failed to poll for access token. Response code: $responseCode")
//            }
//        } catch (e: Exception) {
//            Log.e("GoogleDrive", "Error polling for access token: ${e.message}")
//        }
//
//        return null
//    }

    // Step 3: Upload file to Google Drive
//    private fun uploadFileToGoogleDrive(file: File, callback: (String?) -> Unit) {
//        // Step 1: Request access token using authorization code
//        val authorizationCode = requestAuthorizationCode(CLIENT_ID, CLIENT_SECRET)
//
//        if (authorizationCode != null) {
//            Log.d("GoogleDrive", "Authorization code: $authorizationCode")
//
//            // Step 2: Request access token using the obtained authorization code
//            AsyncTask.execute {
//                val tokenResponse = requestAccessToken(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, authorizationCode)
//
//                if (tokenResponse != null && tokenResponse.has("access_token")) {
//                    val accessToken = tokenResponse.getString("access_token")
//
//                    // Step 3: Upload the file using the obtained access token
//                    uploadFileToGoogleDriveWithToken(file, accessToken, callback)
//                } else {
//                    Log.e("GoogleDrive", "Failed to obtain access token.")
//                    callback(null)
//                }
//            }
//        } else {
//            Log.e("GoogleDrive", "Failed to obtain authorization code.")
//            callback(null)
//        }
//    }
//
//
//    // Step 4: Upload file to Google Drive using the obtained access token
//    private fun uploadFileToGoogleDriveWithToken(file: File, accessToken: String, callback: (String?) -> Unit) {
//        try {
//            // Use the correct endpoint for uploading to Google Drive
//            val uploadUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=media"
//
//            val requestQueue = Volley.newRequestQueue(requireContext())
//
//            val volleyFileUploadRequest = FileUploadRequest(
//                Request.Method.POST,
//                uploadUrl,
//                file,
//                accessToken,
//                { link ->
//                    // File uploaded successfully, 'link' contains the webUrl
//                    // Use the link or perform further actions
//                    Log.d("GoogleDrive", "File uploaded successfully. Link: $link")
//                    callback(link)
//                },
//                { error ->
//                    // Handle error
//                    Log.e("GoogleDrive", "Error uploading file: ${error.message}")
//                    if (error.networkResponse != null) {
//                        Log.e("GoogleDrive", "Response code: ${error.networkResponse.statusCode}")
//                        Log.e("GoogleDrive", "Response data: ${String(error.networkResponse.data)}")
//                    }
//                    callback(null) // Pass null link to indicate failure
//                }
//            )
//
//            // Add the request to the queue
//            requestQueue.add(volleyFileUploadRequest)
//        } catch (e: Exception) {
//            Log.e("GoogleDrive", "Exception during file upload: ${e.message}")
//            e.printStackTrace()
//            callback(null) // Pass null link to indicate failure
//        }
//    }

    // Step 5: Create a temporary file from InputStream
    private fun createTempFileFromInputStream(inputStream: InputStream): File {
        val tempFile = File.createTempFile("temp", null)
        tempFile.deleteOnExit()
        tempFile.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
        return tempFile
    }
    private fun copyFileToFolder(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)

        if (inputStream != null) {
            // Create a folder with the app's name in external storage app-specific directory
            val folderName = "Pertamina"
            val folder = File(requireContext().getExternalFilesDir(null), folderName)

            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    Log.d("bruh", "Folder created at: ${folder.absolutePath}")
                } else {
                    Log.e("bruh", "Failed to create folder at: ${folder.absolutePath}")
                    return null
                }
            } else {
                Log.d("bruh", "Folder already exists at: ${folder.absolutePath}")
            }
            // Provide a specific filename (you may want to replace "yourFileName" with a meaningful name)
            val file = File(folder, filename)
            Log.d("bruh", "File already exists at: ${file.absolutePath}")
            // Copy the content of the input stream to the file
//            inputStream.use { input ->
//                FileOutputStream(file).use { output ->
//                    input.copyTo(output)
//                }
//            }

            // Now, 'file' contains the path to the saved file
            val filePath = file.absolutePath

            // Perform actions with the file path
            Log.d("bruh", "File saved at: $filePath")

            return filePath
        } else {
            Log.e("GoogleDrive", "InputStream is null")
            return null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                Log.d("UploadError", "File URI: $uri")
                fileUri = uri

                // Get the file name from the URI
                val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                val displayName = cursor?.use {
                    it.moveToFirst()
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    it.getString(nameIndex)
                }
                filename = user?.let { generateUniqueFileName(it.nama, displayName ?: "file") }.toString()

                // Update the selectedFileName TextView with the file name
                val selectedFileName = view?.findViewById<TextView>(R.id.selectedFileName)
                selectedFileName?.text = displayName ?: "No file selected"

                // Copy the file to the specified folder
                filepath = copyFileToFolder(uri) ?: "defaultFilePath?"
            }
        } else if (requestCode == CAMERA_CAPTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Set the file name for the captured image
            val imageFileName = tempImageFile?.name ?: "No file selected"
            val selectedFileName = view?.findViewById<TextView>(R.id.selectedFileName)
            selectedFileName?.text = imageFileName
        }
    }

    private fun generateUniqueFileName(userId: String, originalFileName: String): String {
        // You can implement your logic to generate a unique file name
        // Here, I'm using a timestamp to make it unique
        val timestamp = System.currentTimeMillis()
        return "$userId/Lembur/$timestamp/$originalFileName"
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Capture from Camera", "Select from File")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkAndRequestCameraPermission()
                1 -> openFilePicker()
            }
        }
        alertDialog = builder.create()
        alertDialog.show()
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            captureImageFromCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImageFromCamera()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun captureImageFromCamera() {
        tempImageFile = createTempImageFile()

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(
            MediaStore.EXTRA_OUTPUT,
            FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                tempImageFile!!
            )
        )

        startActivityForResult(cameraIntent, CAMERA_CAPTURE_REQUEST)
    }

    private fun createTempImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var uniqueImageFile: File? = null
        var counter = 1

        while (uniqueImageFile == null || uniqueImageFile.exists()) {
            val candidateName = if (counter == 1) {
                imageFileName
            } else {
                "$imageFileName-$counter"
            }
            uniqueImageFile = File(storageDir, "$candidateName.jpg")
            counter++
        }

        return uniqueImageFile
    }


    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var matchingHoliday: Holiday? = null
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, selectedYear, monthOfYear, dayOfMonth ->
                val selectedDate = "$selectedYear-${monthOfYear + 1}-$dayOfMonth"
                matchingHoliday = findMatchingHoliday(selectedDate)
                if (matchingHoliday == null && isDateMatch(selectedDate, selectedYear)) {
                    editText.setText(selectedDate)
                    val dayOfWeek = getDayOfWeek(selectedDate)
                    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                        ketHari = "Libur"
                    } else {
                        ketHari = "Biasa"
                    }
                } else {
                    if (matchingHoliday != null) {
                        ketHari = "Libur"
                        val toastMessage = "Hari Libur ${matchingHoliday?.nama}"
                        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
                        editText.setText(selectedDate)
                    } else {
                        Toast.makeText(requireContext(), "Invalid date", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun getDayOfWeek(selectedDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(selectedDate)

        val calendar = Calendar.getInstance()
        calendar.time = date

        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    private fun isDateMatch(holidayDate: String, year: Int): Boolean {
        val dateParts = holidayDate.split("-")
        if (dateParts.size >= 3) {
            val holidayYear = dateParts[0].toInt()
            return holidayYear == year
        }
        return false
    }

    private fun findMatchingHoliday(selectedDate: String): Holiday? {
        return holidayList.holidayList.find { it.tanggal == selectedDate }
    }

    private fun getData(user: User) {
        // Implementation for retrieving data
    }

    private fun isFormValid(): Boolean {
        val tilTanggal = view?.findViewById<TextInputLayout>(R.id.TITanggal)
        val tilMasuk = view?.findViewById<TextInputLayout>(R.id.TIMasuk)
        val tilKeluar = view?.findViewById<TextInputLayout>(R.id.TIKeluar)

        val isNotBlank = !tilTanggal?.editText?.text.isNullOrBlank() &&
                !tilMasuk?.editText?.text.isNullOrBlank() &&
                !tilKeluar?.editText?.text.isNullOrBlank()

        Log.d("FormValidation", "isNotBlank: $isNotBlank")

        return isNotBlank
    }

//    private fun isImageUploaded(): Boolean {
//        return previewImage.visibility == View.VISIBLE
//    }

}
