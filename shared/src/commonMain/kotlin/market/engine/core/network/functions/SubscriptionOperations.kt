package application.market.agora.business.core.network.functions

import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.AppResponse
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.APIService
import kotlinx.serialization.json.JsonElement

class SubscriptionOperations(private val apiService: APIService) {

    suspend fun postSubOperationsEnable(id: Long?): ServerResponse<Boolean> {
        return try {
            val response = apiService.postSubOperationsEnable(id ?: 1L)
            ServerResponse(success = response.success)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postSubOperationsDisable(id: Long?): ServerResponse<Boolean> {
        return try {
            val response = apiService.postSubOperationsDisable(id ?: 1L)
            ServerResponse(success = response.success)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postSubOperationsDelete(id: Long?): ServerResponse<Boolean> {
        return try {
            val response = apiService.postSubOperationsDelete(id ?: 1L)
            ServerResponse(success = response.success)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOperationsSubscription(id: Long = 1L): ServerResponse<ArrayList<Operations>> {
        return try {
            val response = apiService.getSubscriptionOperations(id)
            try {
                val payload : ArrayList<Operations> = deserializePayload(response.payload)
                ServerResponse(payload)
            }catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOperationsEditSubscription(id: Long): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getSubscriptionsEditSubscription(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
                ServerResponse(payload)
            }catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOperationsEditSubscription(id: Long, body: HashMap<String, JsonElement>): ServerResponse<AppResponse> {
        return try {
            val response= apiService.postSubscriptionsEditSubscription(id, body)
            ServerResponse(response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }
}
