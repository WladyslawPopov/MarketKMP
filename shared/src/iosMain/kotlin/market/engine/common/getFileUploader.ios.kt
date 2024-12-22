package market.engine.common

import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.ServerResponse

actual suspend fun getFileUpload(photoTemp: PhotoTemp): ServerResponse<String> {
    return ServerResponse("")
}
