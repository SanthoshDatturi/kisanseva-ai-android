package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.FileDeleteRequest
import com.kisanseva.ai.domain.model.FileUploadResponse
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.TextToSpeechRequest
import com.kisanseva.ai.domain.state.Result
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
        mimeType: String,
        pathPrefix: String
    ): Result<FileUploadResponse, DataError.Network> {
        return try {
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    blobName,
                    fileStream.readBytes().toRequestBody(mimeType.toMediaType())
                )
                .addFormDataPart("blob_name", blobName)
                .addFormDataPart("file_type", fileType.value)
                .addFormDataPart("path_prefix", pathPrefix)

            val request = Request.Builder()
                .url("$baseUrl/files/")
                .post(requestBodyBuilder.build())
                .build()

            val response = client.newCall(request).await()
            if (!response.isSuccessful) {
                return Result.Error(response.code.toNetworkError())
            }
            val responseBody = response.body.string()
            Result.Success(json.decodeFromString(FileUploadResponse.serializer(), responseBody))
        } catch (_: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    suspend fun textToSpeech(
        request: TextToSpeechRequest
    ): Result<FileUploadResponse, DataError.Network> {
        return try {
            val requestBody = json.encodeToString(TextToSpeechRequest.serializer(), request)
                .toRequestBody("application/json".toMediaType())

            val httpRequest = Request.Builder()
                .url("$baseUrl/files/text-to-speech")
                .post(requestBody)
                .build()

            val response = client.newCall(httpRequest).await()

            if (!response.isSuccessful) {
                return Result.Error(response.code.toNetworkError())
            }

            val responseBody = response.body.string()
            Result.Success(json.decodeFromString(FileUploadResponse.serializer(), responseBody))
        } catch (_: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    suspend fun deleteFile(request: FileDeleteRequest): Result<Unit, DataError.Network> {
        return try {
            val requestBody = json.encodeToString(FileDeleteRequest.serializer(), request)
                .toRequestBody("application/json".toMediaType())

            val httpRequest = Request.Builder()
                .url("$baseUrl/files/")
                .delete(requestBody)
                .build()

            val response = client.newCall(httpRequest).await()

            if (!response.isSuccessful) {
                return Result.Error(response.code.toNetworkError())
            }
            Result.Success(Unit)
        } catch (_: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}
