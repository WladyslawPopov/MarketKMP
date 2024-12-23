package market.engine.common

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.PhotoTemp
import org.jetbrains.compose.resources.getString


actual fun getPhotoPicker(): PhotoPicker {
    return PhotoPickerImpl()
}

var pickMultipleMedia : ActivityResultLauncher<PickVisualMediaRequest>? = null
var callback : CompletableDeferred<List<Uri>> = CompletableDeferred()

class PhotoPickerImpl : PhotoPicker{
    override suspend fun pickImagesRaw(maxCount : Int, maxSizeBytes : Long): List<PhotoTemp> {
        val activity = appContext as ComponentActivity

        pickMultipleMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        val uris = callback.await()

        val photoList = mutableListOf<PhotoTemp>()
        if (uris.isNotEmpty()) {
            for ((index, imageUri) in uris.withIndex()) {
                if (photoList.size >= maxCount) break

                try {
                    val fd = activity.contentResolver.openAssetFileDescriptor(imageUri, "r")
                    val fileSize = fd?.length ?: 0
                    fd?.close()
                    if (fileSize < maxSizeBytes) {
                        photoList.add(
                            PhotoTemp(
                                uri = imageUri.toString(),
                            )
                        )
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                activity,
                                "${getString(strings.sizeLabel)} ${(index + 1)} ${getString(strings.fileTooLarge)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: FileNotFoundException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activity,
                            getString(strings.fileNotFound) + " $imageUri",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        callback = CompletableDeferred()
        return photoList
    }
}
