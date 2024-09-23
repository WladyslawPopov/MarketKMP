package application.market.agora.business.core.network.functions

import market.engine.business.core.ServerErrorException
import market.engine.business.core.ServerResponse
import application.market.agora.business.networkObjects.AdditionalData
import application.market.agora.business.networkObjects.AppResponse
import application.market.agora.business.networkObjects.BodyObj
import application.market.agora.business.networkObjects.BodyPayload
import application.market.agora.business.networkObjects.DynamicPayload
import application.market.agora.business.networkObjects.Fields
import application.market.agora.business.networkObjects.Offer
import application.market.agora.business.networkObjects.OperationResult
import application.market.agora.business.networkObjects.Operations
import application.market.agora.business.networkObjects.PayloadExistence
import application.market.agora.business.networkObjects.deserializePayload
import market.engine.business.core.network.APIService
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive


class OfferOperations(private val apiService: APIService) {

    suspend fun getOffer(id: Long = 1L): ServerResponse<Offer> {
        return try {
            val response = apiService.getOffer(id)
            try {
                val payload =
                    deserializePayload<ArrayList<Offer>>(
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

    suspend fun getOperationsOffer(id: Long = 1L): ServerResponse<ArrayList<Operations>> {
        return try {
            val response = apiService.getOfferOperations(id)
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

    suspend fun postOfferOperationUnwatch(offerId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsUnwatch(offerId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationWatch(offerId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsWatch(offerId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsActivateOfferForFuture(offerId: Long, body: HashMap<String, Long>): ServerResponse<AppResponse> {
        return try {
            val response= apiService.postOfferOperationsActivateOfferForFuture(offerId, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOfferOperationsActivateOffer(offerId: Long): ServerResponse<ArrayList<Fields>> {
        return try {
            val response = apiService.getOfferOperationsActivateOffer(offerId)
            try {
                val payload = deserializePayload<DynamicPayload<Fields>>(response.payload)
                ServerResponse(success = payload.fields)
            }catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsActivateOffer(offerId: Long, body: HashMap<String, String>): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsActivateOffer(offerId, body)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsSetAntiSniper(offerId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsSetAntiSniper(offerId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsUnsetAntiSniper(offerId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsUnsetAntiSniper(offerId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsAddBid(offerId: Long, body: HashMap<String, String>): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsAddBid(offerId, body)
            ServerResponse(success = response)
        }  catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsDeleteOffer(offerId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsDeleteOffer(offerId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsFinalizeSession(offerId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsFinalizeSession(offerId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postOfferOperationsProlongOffer(offerId: Long): ServerResponse<AppResponse> {
        return try {
            val response = apiService.postOfferOperationsProlongOffer(offerId)
            ServerResponse(success = response)
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun postCheckingConversationExistence(id: Long = 1L): ServerResponse<PayloadExistence<AdditionalData>> {
        return try {
            val response = apiService.postCheckingConversationExistenceOffer(id)
            try {
                val payload = deserializePayload<PayloadExistence<AdditionalData>>(response.payload)
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

    suspend fun postGetLeaderAndPrice(id: Long = 1L, version: JsonElement?): ServerResponse<BodyPayload<BodyObj>> {
        return try {
            val body = HashMap<String, String>().apply { put("version", version?.jsonPrimitive?.content ?: "") }
            val response = apiService.postOfferOperationsGetLeaderAndPrice(id, body)
            try {
                val payload =
                    deserializePayload<BodyPayload<BodyObj>>(
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

    suspend fun postWriteToSeller(id: Long = 1L, body: HashMap<String, String>): ServerResponse<PayloadExistence<AdditionalData>> {
        return try {
            val response = apiService.postOfferOperationsWriteToSeller(id, body)
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

    suspend fun getMakeProposal(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getMakeProposal(id)
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

    suspend fun getActOnProposal(id: Long = 1L): ServerResponse<DynamicPayload<OperationResult>> {
        return try {
            val response = apiService.getActOnProposal(id)
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
}
