package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.FileUploadResponse
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.TextToSpeechRequest
import java.io.InputStream

interface FilesRepository {

    suspend fun uploadFile(
        fileStream: InputStream,
        blobName: String,
        fileType: FileType,
        mimeType: String
    ): FileUploadResponse

    suspend fun textToSpeech(
        request: TextToSpeechRequest
    ): FileUploadResponse

    suspend fun deleteFile(url: String, fileType: FileType)
}