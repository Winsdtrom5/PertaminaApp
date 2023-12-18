package com.example.pertaminaapp.session

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class GoogleDriveHelper(private val context: Context, private val clientId: String, private val clientSecret: String) {

    private val TAG = "GoogleDriveHelper"
    private val AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/auth"
    private val TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/token"
    private val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
    private val SCOPE = "https://www.googleapis.com/auth/drive.file"
    private val AUTHORIZATION_URL =
        "$AUTHORIZATION_ENDPOINT?client_id=$clientId&redirect_uri=$REDIRECT_URI&response_type=code&scope=$SCOPE"

    fun authenticate() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AUTHORIZATION_URL))
        context.startActivity(intent)
    }

    fun getAccessToken(authCode: String, callback: (String?) -> Unit) {
        val requestQueue = Volley.newRequestQueue(context)
        val tokenUrl = TOKEN_ENDPOINT

        val requestBody = "code=${URLEncoder.encode(authCode, "UTF-8")}" +
                "&client_id=$clientId" +
                "&client_secret=$clientSecret" +
                "&redirect_uri=$REDIRECT_URI" +
                "&grant_type=authorization_code"

        val request = object : JsonObjectRequest(
            Request.Method.POST, tokenUrl, null,
            Response.Listener { response ->
                try {
                    val accessToken = response.getString("access_token")
                    callback(accessToken)
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception: ${e.message}")
                    callback(null)
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Volley Error: ${error.message}")
                callback(null)
            }
        ) {
            override fun getBody(): ByteArray {
                return try {
                    requestBody.toByteArray(charset("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    Log.e(TAG, "Unsupported Encoding Exception: ${e.message}")
                    byteArrayOf()
                }
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                return params
            }
        }

        requestQueue.add(request)
    }

    // Add functions for uploading files, etc.
}
