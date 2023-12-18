package com.example.pertaminaapp.session

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class FileUploadRequest(
    method: Int,
    url: String,
    private val file: File,
    private val accessToken: String, // Pass the access token directly
    private val successCallback: (String) -> Unit,
    errorListener: Response.ErrorListener
) : Request<JSONObject>(method, url, errorListener) {

    private var mRequestBody: ByteArray? = null

    init {
        buildMultipartEntity()
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        return mRequestBody ?: super.getBody()
    }

    override fun deliverResponse(response: JSONObject) {
        successCallback(response.getString("webUrl")) // Notify success
    }

    private fun buildMultipartEntity() {
        try {
            val bos = ByteArrayOutputStream()
            val fileInputStream = FileInputStream(file)
            val buffer = ByteArray(1024)

            var bytesRead: Int
            while (fileInputStream.read(buffer, 0, buffer.size).also { bytesRead = it } >= 0) {
                bos.write(buffer, 0, bytesRead)
            }
            mRequestBody = bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PROTOCOL_CHARSET = "utf-8"
    }

    override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["Authorization"] = "Bearer $accessToken"
        return headers
    }
    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
        return try {
            val jsonString = String(response.data, charset(HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET)))
            val link = JSONObject(jsonString).getString("webUrl")
            successCallback(link) // Notify success
            Response.success(JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }
}

