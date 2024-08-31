package application.market.auction_mobile.business.core.network.functions

import application.market.auction_mobile.business.core.ServerErrorException
import application.market.auction_mobile.business.core.ServerResponse
import application.market.core.network.APIService

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
