package com.kisanseva.ai.system.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AndroidMediaStorageManager(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient()
) : MediaStorageManager {

    private fun getDir(mimeType: String?): File {
        return when (mimeType?.split('/')?.first()) {
            "image" -> getImageDir()
            "audio" -> getAudioDir()
            else -> context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        }
    }

    private fun getImageDir(): File =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir // fallback

    override fun saveImage(
        bitmap: Bitmap,
        filename: String?,
        mimeType: String
    ): File {
        val dir = getImageDir()
        val finalFilename = filename ?: generateFilename(mimeType)
        val file = File(dir, finalFilename)

        FileOutputStream(file).use { out ->
            val format = when (mimeType) {
                "image/jpeg" -> Bitmap.CompressFormat.JPEG
                else -> Bitmap.CompressFormat.PNG
            }
            bitmap.compress(format, 100, out)
        }

        return file
    }

    override fun saveImage(
        inputStream: InputStream,
        filename: String?,
        mimeType: String?
    ): File {
        require(filename != null || mimeType != null) { "Either filename or mimeType must be provided." }
        val dir = getImageDir()
        val finalFilename = filename ?: generateFilename(mimeType)
        val file = File(dir, finalFilename)

        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }

        return file
    }

    override fun loadImage(path: String): Bitmap? {
        val file = File(path)
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }


    private fun getAudioDir(): File =
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: context.filesDir // fallback

    override fun saveAudio(
        inputStream: InputStream,
        filename: String?,
        mimeType: String?
    ): File {
        require(filename != null || mimeType != null) { "Either filename or mimeType must be provided." }
        val dir = getAudioDir()
        val finalFilename = filename ?: generateFilename(mimeType)
        val file = File(dir, finalFilename)

        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }

        return file
    }

    override fun loadAudio(path: String): File? {
        val file = File(path)
        return if (file.exists()) file else null
    }

    override fun deleteFile(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.delete()
    }

    override fun createNewAudioFile(mimeType: String): File {
        val dir = getAudioDir()
        val filename = generateFilename(mimeType)
        return File(dir, filename)
    }

    override suspend fun downloadToExternalStorage(
        fileUrl: String,
        mimeType: String?
    ): File? = withContext(Dispatchers.IO) {

        val request = Request.Builder().url(fileUrl).build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val inputStream: InputStream = response.body.byteStream()

            // Location: Android/data/<package>/files/<folderName>/
            val directory: File = getDir(mimeType)
            if (!directory.exists()) directory.mkdirs()

            val outFile = File(directory, generateFilename(mimeType))
            val output = FileOutputStream(outFile)

            inputStream.use { input ->
                output.use { fileOut ->
                    input.copyTo(fileOut)
                }
            }

            outFile  // return saved file

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun generateFilename(mimeType: String?): String {
        val extension = when (mimeType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "audio/mpeg" -> ".mp3"
            "audio/mp4" -> ".m4a"
            "audio/wav" -> ".wav"
            else -> ""
        }
        val prefix = when (mimeType?.substringBefore("/")) {
            "image" -> "img"
            "audio" -> "audio"
            else -> "download"
        }
        return "${prefix}_${System.currentTimeMillis()}${extension}"
    }
}
