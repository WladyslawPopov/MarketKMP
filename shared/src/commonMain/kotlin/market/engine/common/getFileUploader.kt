package market.engine.common

import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.ServerResponse

expect suspend fun getFileUpload(photoTemp: PhotoTemp): ServerResponse<String>
