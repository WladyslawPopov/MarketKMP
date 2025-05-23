package market.engine.core.network.functions

import kotlinx.serialization.builtins.ListSerializer
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.UrlBuilder
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload

class OffersListOperations(private val apiService: APIService) {

    suspend fun getOffersListItem(id : Long): ServerResponse<FavoriteListItem> {
        return  try {
            val data = apiService.getOffersListItem(id)
            try {
                val serializer = ListSerializer(FavoriteListItem.serializer())
                val value = deserializePayload(data.payload, serializer)

                return ServerResponse(success = value.firstOrNull())
            }catch (_ : Exception){
                throw ServerErrorException(data.errorCode.toString(), data.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOffersList(): ServerResponse<List<FavoriteListItem>> {
        return  try {
            val url = UrlBuilder()
                .addPathSegment("offers_lists")
                .addPathSegment("get_cabinet_listing")
                .build()

            val data = apiService.getPage(url)
            try {
                val serializer = Payload.serializer(FavoriteListItem.serializer())
                val value = deserializePayload(data.payload, serializer)

                return ServerResponse(success = value.objects)
            }catch (_ : Exception){
                throw ServerErrorException(data.errorCode.toString(), data.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            ServerResponse(error = e)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getOperations(id: Long): ServerResponse<List<Operations>> {
        return try {
            val response = apiService.getOffersListItemOperations(id)
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
}
