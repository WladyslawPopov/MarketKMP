package market.engine.core.network.functions

import kotlinx.serialization.builtins.ListSerializer
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.Order
import market.engine.core.utils.deserializePayload
import market.engine.core.network.APIService

class OrderOperations(private val apiService: APIService) {

    suspend fun getOrder(id: Long = 1L): ServerResponse<Order> {
        return try {
            val response = apiService.getOrder(id)
            try {
                val serializer = ListSerializer(Order.serializer())
                val payload =
                    deserializePayload<List<Order>>(
                        response.payload, serializer
                    )
                ServerResponse(success = payload.firstOrNull())
            }catch (_ : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOperationsOrder(id: Long = 1L): ServerResponse<List<Operations>> {
        return try {
            val response = apiService.getOrderOperations(id)
            try {
                val serializer = ListSerializer(Operations.serializer())
                val payload =
                    deserializePayload<List<Operations>>(
                        response.payload, serializer
                    )
                ServerResponse(success = payload)
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
