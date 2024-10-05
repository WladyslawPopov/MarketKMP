package market.engine.core.network.functions

import kotlinx.coroutines.flow.StateFlow
import market.engine.core.globalData.SD
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.UrlBuilder
import market.engine.core.networkObjects.Category
import market.engine.core.networkObjects.Payload
import market.engine.core.networkObjects.RegionOptions
import market.engine.core.networkObjects.deserializePayload
import market.engine.core.network.APIService

import org.koin.mp.KoinPlatform.getKoin

class CategoryOperations(private val apiService : APIService) {

    val searchData : StateFlow<SD> = getKoin().get()

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
        val sd = searchData.value.copy()
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

