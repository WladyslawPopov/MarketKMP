package market.engine.core.network.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import market.engine.common.getFileUpload
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse

suspend fun uploadFile(photoTemp: PhotoTemp) : ServerResponse<String> {
    try {
        val res = withContext(Dispatchers.IO) {
            getFileUpload(photoTemp)
        }

        return withContext(Dispatchers.Main) {
            val cleanedSuccess = res.success?.trimStart('[')?.trimEnd(']')?.replace("\"", "")
            photoTemp.tempId = cleanedSuccess
            ServerResponse(cleanedSuccess)
        }
    } catch (e : ServerErrorException){
        return withContext(Dispatchers.Main) {
            ServerResponse(error = e)
        }
    }catch (e : Exception){
        return withContext(Dispatchers.Main) {
            ServerResponse(error = ServerErrorException(errorCode = e.message ?: ""))
        }
    }
}
