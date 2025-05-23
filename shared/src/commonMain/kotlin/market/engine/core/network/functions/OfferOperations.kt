package market.engine.core.network.functions

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Operations
import market.engine.core.utils.deserializePayload
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.BodyObj
import market.engine.core.network.networkObjects.BodyPayload

class OfferOperations(private val apiService: APIService) {

    suspend fun getOffer(id: Long): ServerResponse<Offer> {
        return try {
            val response = apiService.getOffer(id)
            try {
                val serializer = ListSerializer(Offer.serializer())
                val payload =
                    deserializePayload(
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

    suspend fun getOperationsOffer(id: Long, tag: String): ServerResponse<List<Operations>> {
        return try {
            val response = apiService.getOfferOperations(id, tag)
            try {
                val serializer = ListSerializer(Operations.serializer())
                val payload =
                    deserializePayload(
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

    suspend fun postGetLeaderAndPrice(id: Long = 1L, version: JsonElement?): ServerResponse<BodyPayload<BodyObj>> {
        return try {
            val body = HashMap<String, String>().apply { put("version", version?.jsonPrimitive?.content ?: "") }
            val response = apiService.postOfferOperationsGetLeaderAndPrice(id, body)
            try {
                val serializer = BodyPayload.serializer(BodyObj.serializer())
                val payload =
                    deserializePayload(
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
