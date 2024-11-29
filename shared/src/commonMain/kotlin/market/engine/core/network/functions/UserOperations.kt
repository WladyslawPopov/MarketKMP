package application.market.agora.business.core.network.functions

import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.AdditionalDataForNewOrder
import market.engine.core.network.networkObjects.AddressCards
import market.engine.core.network.networkObjects.AppResponse
import market.engine.core.network.networkObjects.BodyObj
import market.engine.core.network.networkObjects.BodyPayload
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.PayloadExistence
import market.engine.core.network.networkObjects.User
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.APIService
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import market.engine.core.network.networkObjects.ListData

class UserOperations(val apiService: APIService) {

    suspend fun getUsers(idUser: Long): ServerResponse<ArrayList<User>> {
        return try {
            val response = apiService.getUsers(idUser)
            try {
                val payload = deserializePayload<ArrayList<User>>(response.payload)
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

    suspend fun postUserOperationsCreateSubscription(
        id: Long = 1L,
        body: HashMap<String, JsonElement>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUserCreateSubscription(id, body)
            ServerResponse(response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getUserOperationsCreateSubscription(idUser: Long): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUserCreateSubscription(idUser)
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

    suspend fun postUsersOperationDeleteCart(id: Long): ServerResponse<Boolean> {
        return try {
            val res = apiService.postUsersOperationDeleteCart(id)
            if (res.success){
                ServerResponse(success = true)
            }else{
                throw ServerErrorException(res.errorCode.toString(), res.humanMessage.toString())
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
                val payload : BodyPayload<ListData> = deserializePayload(response.payload)
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


    suspend fun postUsersOperationsAddItemToCart(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsAddItemToCart(id, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsRemoveManyItemsFromCart(
        id: Long = 1L,
        body: JsonObject
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsRemoveManyItemsFromCart(id, body)
            ServerResponse(success = response)
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
                val payload =
                    deserializePayload<PayloadExistence<AdditionalDataForNewOrder>>(
                        response.payload
                    )
                ServerResponse(success = payload)
            } catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsSetAvatar(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsSetAvatar(id, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getUsersOperationsSetLogin(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetLogin(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
                ServerResponse(success = payload)
            } catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsSetLogin(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetLogin(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetBiddingStep(
        id: Long = 1L,
        body: HashMap<String, Int>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetBiddingStep(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetVacation(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetVacation(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetWatermarkEnabled(
        id: Long = 1L
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetWatermarkEnabled(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetWatermarkDisabled(
        id: Long = 1L
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetWatermarkDisabled(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetEmail(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetEmail(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetPhone(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetPhone(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetMessageToBuyer(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetMessageToBuyer(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetAutoFeedback(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetAutoFeedback(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetBiddingStep(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetBiddingStep(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetVacation(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetVacation(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetWatermark(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetWatermark(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsAddressCards(id: Long = 1L): ServerResponse<BodyPayload<AddressCards>> {
        return try {
            val response = apiService.getUsersOperationsAddressCards(id)
            try {
                val payload : BodyPayload<AddressCards> = deserializePayload(response.payload)
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


    suspend fun getUsersOperationsSetAddressCards(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetAddressCards(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetAddressCards(
        id: Long = 1L,
        body: JsonObject
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetAddressCards(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetOutgoingAddress(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetOutgoingAddress(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetMessageToBuyer(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetMessageToBuyer(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetOutgoingAddress(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetOutgoingAddress(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetPhone(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetPhone(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetPassword(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetPassword(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsResetPassword(): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsResetPassword()
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetPassword(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetPassword(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsResetPassword(body: HashMap<String, String>): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsResetPassword(body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetEmail(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetEmail(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetAutoFeedback(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsSetAutoFeedback(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun getUsersOperationsSetAboutMe(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsEditAboutMe(id)
            try{
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
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

    suspend fun postUsersOperationsSetAboutMe(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.postUsersOperationsEditAboutMe(id, body)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response?.payload)
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

    suspend fun postUsersOperationsUnsetAvatar(id: Long = 1L): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsUnsetAvatar(id)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsSetGender(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsSetGender(id, body)
            ServerResponse(success = response)
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
                val payload =
                    deserializePayload<BodyPayload<BodyObj>>(
                        response.payload
                    )
                ServerResponse(success = payload)
            } catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsRAC(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsRAC(id, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsEACC(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsEACC(id, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsVerifyPhone(
        id: Long = 1L,
        body: HashMap<String, String>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsVerifyPhone(id, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getUsersOperationsSetGender(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getUsersOperationsSetGender(id)
            try {
                val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload)
                ServerResponse(success = payload)
            } catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsSetAddressCardsDefault(
        id: Long = 1L,
        body: HashMap<String, Long>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsSetDefaultAddressCard(id, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUsersOperationsDeleteAddressCards(
        id: Long = 1L,
        body: HashMap<String, Long>
    ): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUsersOperationsDeleteAddressCard(id, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }
}
