package com.kisanseva.ai.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kisanseva.ai.R
import java.io.InputStream

@Composable
fun rememberGalleryLauncher(
    currentImageCount: Int,
    onImageSelected: (InputStream, String) -> Unit
): ManagedActivityResultLauncher<PickVisualMediaRequest, List<Uri>> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = PickMultipleVisualMedia(5),
        onResult = { uris ->
            if (currentImageCount >= 5) {
                Toast.makeText(context, context.getString(R.string.max_images_error), Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            val availableSlots = 5 - currentImageCount
            if (uris.size > availableSlots) {
                Toast.makeText(context, context.getString(R.string.limit_images_info, availableSlots), Toast.LENGTH_LONG).show()
            }
            uris.take(availableSlots).forEach { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val mimeType = context.contentResolver.getType(uri)
                if (inputStream != null && mimeType != null) {
                    onImageSelected(inputStream, mimeType)
                }
            }
        }
    )
}
