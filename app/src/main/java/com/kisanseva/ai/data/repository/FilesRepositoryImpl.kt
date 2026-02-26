package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.remote.FilesApi
import com.kisanseva.ai.domain.model.FileDeleteRequest
import com.kisanseva.ai.domain.model.FileUploadResponse
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.TextToSpeechRequest
import com.kisanseva.ai.domain.repository.FilesRepository
import java.io.InputStream

class FilesRepositoryImpl(
    private val filesApi: FilesApi
) : FilesRepository {

    override suspend fun uploadFile(
        fileStream: InputStream,
        blobName: String,
        fileType: FileType,
        mimeType: String,
        pathPrefix: String
    ): FileUploadResponse {
        return filesApi.uploadFile(fileStream, blobName, fileType, mimeType, pathPrefix)
    }

    override suspend fun textToSpeech(
        request: TextToSpeechRequest
    ): FileUploadResponse {
        return filesApi.textToSpeech(request)
    }

    override suspend fun deleteFile(url: String, fileType: FileType?) {
        filesApi.deleteFile(FileDeleteRequest(url, fileType))
    }
}
