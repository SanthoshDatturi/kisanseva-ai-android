package com.kisanseva.ai.system.storage

import android.graphics.Bitmap
import java.io.File
import java.io.InputStream

interface MediaStorageManager {

    // IMAGE STORAGE
    fun saveImage(
        bitmap: Bitmap,
        filename: String? = null,
        mimeType: String = "image/png"
    ): File

    fun saveImage(
        inputStream: InputStream,
        filename: String? = null,
        mimeType: String? = null
    ): File

    fun loadImage(path: String): Bitmap?


    // AUDIO STORAGE
    fun saveAudio(
        inputStream: InputStream,
        filename: String? = null,
        mimeType: String? = null
    ): File

    fun loadAudio(path: String): File?

    fun createNewAudioFile(mimeType: String = "audio/mp4"): File

    fun deleteFile(path: String): Boolean

    suspend fun downloadToExternalStorage(
        fileUrl: String,
        mimeType: String?
    ): File?
}
