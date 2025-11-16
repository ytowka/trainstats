package com.danilkha.trainstats.features.settings.workoutimport.data

import android.content.ContentResolver
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class FileReader @Inject constructor(
    private val contentResolver: ContentResolver
){

    fun readFile(fileUri: Uri): String {
        return contentResolver.openInputStream(fileUri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalArgumentException("Unable to open URI: $fileUri")
    }
}