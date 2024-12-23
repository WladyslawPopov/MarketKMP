package market.engine.core.network.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import market.engine.common.getFileUpload
import market.engine.core.data.items.PhotoTemp

suspend fun uploadFile(photoTemp: PhotoTemp) : String? {
        val res = withContext(Dispatchers.IO) {
            getFileUpload(photoTemp)
        }

        return withContext(Dispatchers.Main) {
            res.success
        }
    }
