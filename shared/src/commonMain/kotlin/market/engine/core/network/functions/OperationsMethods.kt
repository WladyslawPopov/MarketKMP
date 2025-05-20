package market.engine.core.network.functions

import kotlinx.serialization.json.JsonElement
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.utils.deserializePayload

class OperationsMethods(private val apiService: APIService) {
    suspend fun getOperationFields(id: Long, operation : String, method: String): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getOperationFields(id, operation, method)
            try {
                val serializer = DynamicPayload.serializer(OperationResult.serializer())
                val payload: DynamicPayload<OperationResult> =
                    deserializePayload(response.payload, serializer)
                ServerResponse(success = payload)
            } catch (_: Exception) {
                throw ServerErrorException(
                    response.errorCode.toString(),
                    response.humanMessage.toString()
                )
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOperation(
        id: Long = 1L,
        operation : String,
        method: String,
        body: HashMap<String, JsonElement> = hashMapOf()
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postOperation(id,operation, method, body)
            try {
                val serializer = DynamicPayload.serializer(OperationResult.serializer())
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
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
