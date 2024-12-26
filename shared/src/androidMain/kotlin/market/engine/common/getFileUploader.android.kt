package market.engine.common

import market.engine.core.network.ServerResponse
import market.engine.core.data.items.PhotoTemp


actual suspend fun getFileUpload(photoTemp: PhotoTemp): ServerResponse<String> {
    val fileUpload = FileUpload()
    return fileUpload.uploadFile(photoTemp)
}


