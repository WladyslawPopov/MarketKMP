package application.market.agora.business.core.network.functions

import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.networkObjects.AdditionalData
import market.engine.core.networkObjects.AppResponse
import market.engine.core.networkObjects.BodyObj
import market.engine.core.networkObjects.BodyPayload
import market.engine.core.networkObjects.DynamicPayload
import market.engine.core.networkObjects.Fields
import market.engine.core.networkObjects.Offer
import market.engine.core.networkObjects.OperationResult
import market.engine.core.networkObjects.Operations
import market.engine.core.networkObjects.PayloadExistence
import market.engine.core.networkObjects.deserializePayload
import market.engine.core.network.APIService
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
