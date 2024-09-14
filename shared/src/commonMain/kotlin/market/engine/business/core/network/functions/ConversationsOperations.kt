package application.market.auction_mobile.business.core.network.functions


import market.engine.business.core.ServerErrorException
import application.market.auction_mobile.business.networkObjects.Conversations
import application.market.auction_mobile.business.networkObjects.deserializePayload
import market.engine.business.core.network.APIService
import kotlinx.serialization.json.JsonObject

class ConversationsOperations(private val apiService : APIService) {


    suspend fun getConversation(id: Long = 1L): Conversations? {
        return try {
            val response = apiService.getConversation(id)
            try {
                val payload =
                    deserializePayload<ArrayList<Conversations>>(
                        response.payload
                    )
                payload.firstOrNull()
            }catch (e : Exception){
                null
            }
        } catch (e: ServerErrorException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun postMarkAsReadByInterlocutor(id: Long = 1L): Boolean {
        return try {
            val response = apiService.postConversationsOperationsMarkAsReadByInterlocutor(id)
            response.errorCode == ""
        } catch (e: ServerErrorException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun postDeleteForInterlocutor(id: Long = 1L): String? {
        return try {
            val response = apiService.postConversationsOperationsDeleteForInterlocutor(id)
            if (response.success) {
                true.toString()
            } else {
                response.errorCode
            }
        } catch (e: ServerErrorException) {
            e.humanMessage
        } catch (e: Exception) {
            e.message
        }
    }

    suspend fun postAddMessage(id: Long = 1L, body: JsonObject): String? {
        return try {
            val response = apiService.postConversationOperationsAddMessage(id, body)
            if (response.success) {
                true.toString()
            } else {
                response.humanMessage
            }
        } catch (e: ServerErrorException) {
            e.humanMessage
        } catch (e: Exception) {
            e.message
        }
    }
}
