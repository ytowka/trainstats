package com.danilkha.trainstats.features.settings.export.data

import android.content.ContentResolver
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore

class FileWriter(
    val contentResolver: ContentResolver
){

    fun writeFile(fileName: String, content: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw Exception("supports only android 10 and above")
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }


        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = contentResolver.insert(collection, contentValues)

        itemUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }
    }
}