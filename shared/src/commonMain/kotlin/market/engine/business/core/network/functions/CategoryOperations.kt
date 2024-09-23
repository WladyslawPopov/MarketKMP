package market.engine.business.core.network.functions

import market.engine.business.core.ServerErrorException
import market.engine.business.core.ServerResponse
import market.engine.business.core.UrlBuilder
import application.market.agora.business.networkObjects.Category
import application.market.agora.business.networkObjects.Payload
import application.market.agora.business.networkObjects.RegionOptions
import application.market.agora.business.networkObjects.deserializePayload
import market.engine.business.core.network.APIService
import market.engine.business.globalObjects.searchData

class CategoryOperations(private val apiService : APIService) {

    suspend fun getCategoryInfo(id: Long?): ServerResponse<Category> {
        return try {
            val response = apiService.getPublicCategory(id ?: 1L)
            try {
                val payload =
                    deserializePayload<ArrayList<Category>>(
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

    suspend fun getTotalCount(id: Long): ServerResponse<Int> {
        val sd = searchData.copy()
        sd.searchCategoryID = id
        val url = UrlBuilder()
            .addPathSegment("offers")
            .addPathSegment("get_public_listing_counter")
            .addFilters(null, sd)
            .build()

        try {
            val response = apiService.getPageOffers(url)
            try {
                val payload: Payload<RegionOptions> = deserializePayload(response.payload!!)
                return ServerResponse(payload.totalCount)
            }catch (e : Exception){
                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
            }
        } catch (e: ServerErrorException) {
            return ServerResponse(error = e)
        } catch (e: Exception) {
            return ServerResponse(error = ServerErrorException(e.message.toString(), ""))
        }
    }

    suspend fun getRegions(): ArrayList<RegionOptions>? {
        try {
            val response = apiService.getTaggedBy("region")
            if(response.payload != null) {
                try {
                    val payload: Payload<RegionOptions> =
                        deserializePayload(response.payload)
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

