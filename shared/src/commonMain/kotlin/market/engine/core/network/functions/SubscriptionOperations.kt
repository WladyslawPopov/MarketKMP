package market.engine.core.network.functions

import kotlinx.serialization.builtins.ListSerializer
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.Operations
import market.engine.core.utils.deserializePayload
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Subscription

class SubscriptionOperations(private val apiService: APIService) {

    suspend fun getSubscription(id: Long): ServerResponse<Subscription?> {
        return try {
            val response = apiService.getSubscription(id)
            try {
                val serializer = ListSerializer(Subscription.serializer())
                val payload : List<Subscription> = deserializePayload(response.payload, serializer)
                ServerResponse(payload.firstOrNull())
            }catch (_ : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOperationsSubscription(id: Long = 1L): ServerResponse<List<Operations>> {
        return try {
            val response = apiService.getSubscriptionOperations(id)
            try {
                val serializer = ListSerializer(Operations.serializer())
                val payload : List<Operations> = deserializePayload(response.payload, serializer)
                ServerResponse(payload)
            }catch (_ : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }
}
