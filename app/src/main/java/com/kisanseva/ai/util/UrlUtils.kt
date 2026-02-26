package com.kisanseva.ai.util

import android.net.Uri
import com.kisanseva.ai.BuildConfig
import androidx.core.net.toUri

object UrlUtils {
    private const val BLOB_BASE_URL = BuildConfig.BLOB_BASE_URL

    /**
     * Converts a blob reference to a full URL when the container is already part of the ref or known.
     * Example: if blobRef is "ai-chat/images/photo.jpg"
     */
    fun getFullUrlFromRef(blobRef: String): String {
        return "$BLOB_BASE_URL/${encodePath(blobRef)}"
    }

    private fun encodePath(path: String): String {
        return path.split("/").joinToString("/") { Uri.encode(it, null) }
    }
}
