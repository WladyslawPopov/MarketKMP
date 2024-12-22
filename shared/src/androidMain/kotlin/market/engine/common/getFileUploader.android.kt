package market.engine.common

import market.engine.core.network.ServerResponse
import android.net.Uri
import market.engine.core.data.items.PhotoTemp


actual suspend fun getFileUpload(photoTemp: PhotoTemp): ServerResponse<String> {
    val fileUpload = FileUpload()
    val uri = photoTemp.uri?.let { Uri.parse(it) }
    return fileUpload.uploadFile(uri, "image.jpg", "image/jpeg", photoTemp.url)
}


