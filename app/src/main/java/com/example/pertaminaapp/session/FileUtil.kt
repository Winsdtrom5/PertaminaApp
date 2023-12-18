package com.example.pertaminaapp.session

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import java.io.File

object FileUtil {
    fun getPath(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        var filePath: String? = null

        // Try to query for the file path directly
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (columnIndex != -1) {
                    filePath = it.getString(columnIndex)
                }
            }
        }

        // If the file path is still null, try using the DocumentFile API
        if (filePath == null) {
            val document = DocumentFile.fromSingleUri(context, uri)
            document?.let {
                filePath = it.uri.toString()
            }
        }

        return filePath
    }

    private fun getDataColumn(
        context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?
    ): String? {
        val cursor = context.contentResolver.query(
            uri, arrayOf(MediaStore.Images.Media.DATA), selection, selectionArgs, null
        )

        return try {
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.getString(column_index)
            } else {
                null
            }
        } finally {
            cursor?.close()
        }
    }
}

