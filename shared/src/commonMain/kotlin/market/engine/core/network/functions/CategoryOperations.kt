package market.engine.core.network.functions

import kotlinx.serialization.builtins.ListSerializer
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.UrlBuilder
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.RegionOptions
import market.engine.core.network.APIService
import market.engine.core.utils.deserializePayload


class CategoryOperations(private val apiService : APIService) {

    suspend fun getCategoryInfo(id: Long?): ServerResponse<Category> {
        return try {
            val response = apiService.getPublicCategory(id ?: 1L)
            try {
                val categoryListSerializer = ListSerializer(Category.serializer())
                val payload =
                    deserializePayload(
                        response.payload, categoryListSerializer
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

    suspend fun getTotalCount(searchData: SD, listingData: LD): ServerResponse<Int> {
        val sd = searchData.copy()
        val ld = listingData.copy()
        val url = UrlBuilder()
            .addPathSegment("offers")
            .addPathSegment("get_public_listing_counter")
            .addFilters(ld, sd)
            .build()

        try {
            val response = apiService.getPage(url)
            try {

                val serializer = Payload.serializer(RegionOptions.serializer())
                val payload: Payload<RegionOptions> = deserializePayload(response.payload!!, serializer)
                return ServerResponse(payload.totalCount)
            }catch (_ : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            return ServerResponse(error = e)
        } catch (e: Exception) {
            return ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getRegions(): List<RegionOptions>? {
        try {
            val response = apiService.getTaggedBy("region")
            if(response.payload != null) {
                try {
                    val serializer = Payload.serializer(RegionOptions.serializer())
                    val payload: Payload<RegionOptions> =
                        deserializePayload(response.payload, serializer)
                    return payload.objects
                }catch (_ : Exception){
                    return null
                }
            }else{
                return null
            }
        } catch (_: Exception) {
            return null
        }
    }
}
