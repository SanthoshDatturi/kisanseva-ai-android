package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.FileDeleteRequest
import com.kisanseva.ai.domain.model.FileUploadResponse
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.TextToSpeechRequest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.InputStream

class FilesApi(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val json: Json
) {

    suspend fun uploadFile(
        fileStream: InputStream,
        blobName: String,
        fileType: FileType,
        mimeType: String
    ): FileUploadResponse {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                blobName,
                fileStream.readBytes().toRequestBody(mimeType.toMediaType())
            )
            .addFormDataPart("blob_name", blobName)
            .addFormDataPart("file_type", fileType.name.lowercase())
            .build()

        val request = Request.Builder()
            .url("$baseUrl/files/upload")
            .post(requestBody)
            .build()

        val response = client.newCall(request).await()
        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }
        val responseBody = response.body.string()
        return json.decodeFromString(FileUploadResponse.serializer(), responseBody)
    }

    suspend fun textToSpeech(
        request: TextToSpeechRequest
    ): FileUploadResponse {
        val requestBody = json.encodeToString(TextToSpeechRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("$baseUrl/files/text-to-speech")
            .post(requestBody)
            .build()

        val response = client.newCall(httpRequest).await()

        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }

        val responseBody = response.body.string()
        return json.decodeFromString(FileUploadResponse.serializer(), responseBody)
    }

    suspend fun deleteFile(request: FileDeleteRequest) {
        val requestBody = json.encodeToString(FileDeleteRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("$baseUrl/files/")
            .delete(requestBody)
            .build()

        val response = client.newCall(httpRequest).await()

        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }
    }
}
