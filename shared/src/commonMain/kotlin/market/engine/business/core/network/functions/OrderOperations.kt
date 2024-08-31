package application.market.auction_mobile.business.core.network.functions

import application.market.auction_mobile.business.core.ServerErrorException
import application.market.auction_mobile.business.core.ServerResponse
import application.market.auction_mobile.business.networkObjects.AdditionalData
import application.market.auction_mobile.business.networkObjects.AppResponse
import application.market.auction_mobile.business.networkObjects.Operations
import application.market.auction_mobile.business.networkObjects.Order
import application.market.auction_mobile.business.networkObjects.PayloadExistence
import application.market.auction_mobile.business.networkObjects.deserializePayload
import application.market.core.network.APIService

class OrderOperations(private val apiService: APIService) {

    suspend fun getOrder(id: Long = 1L): ServerResponse<Order> {
        return try {
            val response = apiService.getOrder(id)
            try {
                val payload =
                    deserializePayload<ArrayList<Order>>(
                        response.payload
                    )
                ServerResponse(success = payload.firstOrNull())
            }catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOperationsOrder(id: Long = 1L): ServerResponse<ArrayList<Operations>> {
        return try {
            val response = apiService.getOrderOperations(id)
            try {
                val payload =
                    deserializePayload<ArrayList<Operations>>(
                        response.payload
                    )
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

    suspend fun postUnMarkAsParcelSent(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUnMarkAsParcelSent(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postMarkAsParcelSent(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postMarkAsParcelSent(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUnMarkAsPaymentSent(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUnMarkAsPaymentSent(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postMarkAsPaymentSent(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postMarkAsPaymentSent(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postMarkAsArchivedBySeller(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postMarkAsArchivedBySeller(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postUnMarkAsArchivedBySeller(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postUnMarkAsArchivedBySeller(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postEnableFeedbacks(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postEnableFeedbacks(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postDisableFeedbacks(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postDisableFeedbacks(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postMarkAsArchivedByBuyer(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postMarkAsArchivedByBuyer(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postRemoveFeedbackToBuyer(orderId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postRemoveFeedbackToBuyer(orderId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOrderOperationsWriteToPartner(id: Long = 1L, body: HashMap<String, String>): ServerResponse<PayloadExistence<AdditionalData>> {
        return try {
            val response= apiService.postOrderOperationsWriteToPartner(id, body)
            try {
                val payload =
                    deserializePayload<PayloadExistence<AdditionalData>>(
                        response.payload
                    )
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

    suspend fun postSetComment(id: Long = 1L, body: HashMap<String, String>): ServerResponse<PayloadExistence<AdditionalData>> {
        return try {
            val response = apiService.postOrderOperationsSetComment(id, body)
            try {
                val payload =
                    deserializePayload<PayloadExistence<AdditionalData>>(
                        response.payload
                    )
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

    suspend fun postGiveFeedbackToBuyer(id: Long = 1L, body: HashMap<String, String>): ServerResponse<PayloadExistence<AdditionalData>> {
        return try {
            val response = apiService.postOrderOperationsGiveFeedbackToBuyer(id, body)
            try {
                val payload =
                    deserializePayload<PayloadExistence<AdditionalData>>(
                        response.payload
                    )
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

    suspend fun postGiveFeedbackToSeller(id: Long = 1L, body: HashMap<String, String>): ServerResponse<PayloadExistence<AdditionalData>> {
        return try {
            val response = apiService.postOrderOperationsGiveFeedbackToSeller(id, body)
            try {
                val payload =
                    deserializePayload<PayloadExistence<AdditionalData>>(
                        response.payload
                    )
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

    suspend fun postProvideTrackId(id: Long = 1L, body: HashMap<String, String>): ServerResponse<PayloadExistence<AdditionalData>> {
        return try {
            val response = apiService.postOrderOperationsProvideTrackId(id, body)
            val payload =
                deserializePayload<PayloadExistence<AdditionalData>>(
                    response.payload
                )
            ServerResponse(success = payload)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }
}
