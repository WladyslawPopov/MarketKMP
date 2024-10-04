package application.market.agora.business.core.network.functions

import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.APIService

class PrivateMessagesOperation(private val apiService : APIService) {

    suspend fun postDeleteForInterlocutor(id: Long=1L): ServerResponse<Boolean> {
        try {
            val response = apiService.postPMOperationDeleteForInterlocutor(id)
            return ServerResponse(response.success)
        }
        catch (e : ServerErrorException){
            return ServerResponse(error = e)
        }catch (e: Exception) {
            return ServerResponse(
                error = ServerErrorException(
                    e.message.toString(),
                    ""
                )
            )
        }
    }
}
