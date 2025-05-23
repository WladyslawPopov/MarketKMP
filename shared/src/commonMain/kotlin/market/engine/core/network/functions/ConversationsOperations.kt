package market.engine.core.network.functions


import kotlinx.serialization.builtins.ListSerializer
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.utils.deserializePayload
import market.engine.core.network.APIService
import kotlinx.serialization.json.JsonObject
import market.engine.core.network.ServerResponse

class ConversationsOperations(private val apiService : APIService) {

    suspend fun getConversation(id: Long = 1L): ServerResponse<Conversations?> {
        try {
            val response = apiService.getConversation(id)
            if (response.success) {
                val serializer = ListSerializer(Conversations.serializer())
                val payload =
                    deserializePayload(
                        response.payload, serializer
                    )
                return ServerResponse(payload.firstOrNull())
            }else{
                throw ServerErrorException(
                    errorCode = response.errorCode ?: "",
                    humanMessage = response.humanMessage ?: ""
                )
            }
        } catch (e: ServerErrorException) {
            return ServerResponse(error = e)
        } catch (e: Exception) {
            return  ServerResponse( error = ServerErrorException(
                errorCode = "error",
                humanMessage = e.message ?: ""
            ))
        }
    }

    suspend fun postMarkAsReadByInterlocutor(id: Long = 1L): Boolean {
        return try {
            val response = apiService.postOperation(id, "mark_as_read_by_interlocutor", "conversations", emptyMap())
            response.errorCode == ""
        } catch (_: ServerErrorException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun postDeleteForInterlocutor(id: Long = 1L): String? {
        return try {
            val response = apiService.postOperation(id, "delete_for_interlocutor", "conversations", emptyMap())
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
            val response = apiService.postOperation(id, "add_message", "conversations", body)
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
