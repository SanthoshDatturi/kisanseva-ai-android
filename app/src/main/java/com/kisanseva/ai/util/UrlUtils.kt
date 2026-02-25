package com.kisanseva.ai.util

import com.kisanseva.ai.BuildConfig

object UrlUtils {
    private const val AZURE_BLOB_BASE_URL = BuildConfig.BLOB_BASE_URL


    /**
     * Converts a blob reference to a full URL.
     * @param container The container name (e.g., ai-chat, user-content).
     * @param blobRef The blob reference (e.g., images/userId/chatId/photo.jpg).
     * @return The full URL to the blob.
     */
    fun getFullUrl(container: String, blobRef: String): String {
        if (blobRef.startsWith("http")) return blobRef
        return "$AZURE_BLOB_BASE_URL/$container/$blobRef"
    }

    /**
     * Converts a blob reference to a full URL when the container is already part of the ref or known.
     * Example: if blobRef is "ai-chat/images/photo.jpg"
     */
    fun getFullUrlFromRef(blobRef: String): String {
        if (blobRef.startsWith("http")) return blobRef
        return "$AZURE_BLOB_BASE_URL/$blobRef"
    }
}
