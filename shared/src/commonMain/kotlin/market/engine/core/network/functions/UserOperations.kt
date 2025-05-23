package market.engine.core.network.functions

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.AdditionalDataForNewOrder
import market.engine.core.network.networkObjects.AddressCards
import market.engine.core.network.networkObjects.BodyObj
import market.engine.core.network.networkObjects.BodyPayload
import market.engine.core.network.networkObjects.PayloadExistence
import market.engine.core.network.networkObjects.User
import market.engine.core.utils.deserializePayload
import market.engine.core.network.APIService
import kotlinx.serialization.json.JsonObject
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.ListData
import market.engine.core.network.networkObjects.OperationResult

class UserOperations(val apiService: APIService) {

    suspend fun getUsers(idUser: Long): ServerResponse<List<User>> {
        return try {
            val response = apiService.getUsers(idUser)
            try {
                val serializer = ListSerializer(User.serializer())
                val payload = deserializePayload(response.payload, serializer)
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

    suspend fun getUsersOperationsGetUserList(id: Long = 1L, body: HashMap<String, String>): ServerResponse<BodyPayload<ListData>> {
        return try {
            val response = apiService.postUserList(id, body)
            try {
                val serializer = BodyPayload.serializer(ListData.serializer())
                val payload : BodyPayload<ListData> = deserializePayload(response.payload,serializer)
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

    suspend fun postUserOperationsGetAdditionalDataBeforeCreateOrder(
        id: Long?,
        body: JsonObject
    ): ServerResponse<PayloadExistence<AdditionalDataForNewOrder>> {
        return try {
            val response =
                apiService.postUserOperationsGetAdditionalDataBeforeCreateOrder(id ?: 1L, body)
            try {
                val serializer = PayloadExistence.serializer(AdditionalDataForNewOrder.serializer())
                val payload =
                    deserializePayload(
                        response.payload, serializer
                    )
                ServerResponse(success = payload)
            } catch (_ : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getUsersOperationsAddressCards(id: Long = 1L): ServerResponse<BodyPayload<AddressCards>> {
        return try {
            val response = apiService.getUsersOperationsAddressCards(id)
            try {
                val serializer = BodyPayload.serializer(AddressCards.serializer())
                val payload : BodyPayload<AddressCards> = deserializePayload(response.payload, serializer)
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

    suspend fun getUsersOperationsResetPassword(): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsResetPassword()
            try {
                val serializer = DynamicPayload.serializer(OperationResult.serializer())
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                ServerResponse(success = payload)
            }catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsResetPassword(body: HashMap<String, JsonElement>): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsResetPassword(body)
            try {
                val serializer = DynamicPayload.serializer(OperationResult.serializer())
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                ServerResponse(success = payload)
            }catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsConfirmEmail(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<BodyPayload<BodyObj>> {
        return try {
            val response = apiService.postUsersOperationsConfirmEmail(id, body)
            try {
                val serializer = BodyPayload.serializer(BodyObj.serializer())
                val payload =
                    deserializePayload(
                        response.payload, serializer
                    )
                ServerResponse(success = payload)
            } catch (_ : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }
}
