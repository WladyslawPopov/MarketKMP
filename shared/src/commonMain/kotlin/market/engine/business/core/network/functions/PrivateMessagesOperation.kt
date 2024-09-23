package application.market.agora.business.core.network.functions

import market.engine.business.core.ServerErrorException
import market.engine.business.core.ServerResponse
import market.engine.business.core.network.APIService

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
