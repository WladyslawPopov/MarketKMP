package market.engine.core.network.paging.offer

import androidx.paging.PagingSource
import androidx.paging.PagingState
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.UrlBuilder
import market.engine.core.network.APIService
import market.engine.core.items.ListingData

open class OfferPagingSource(private val apiService: APIService, private val listingData: ListingData) :
    PagingSource<Int, Offer>() {

    override fun getRefreshKey(state: PagingState<Int, Offer>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> =
        (params.key ?: listingData.data.value.pgCount).let { page ->
            try {
                val url = UrlBuilder()
                    .addPathSegment("offers")
                    .addPathSegment(listingData.data.value.methodServer)
                    .addQueryParameter("pg", page.toString())
                    .addQueryParameter("ipp", params.loadSize.toString())
                    .addFilters(listingData.data.value,listingData.searchData.value)
                    .build()

                val data = apiService.getPageOffers(url)

                if (data.success) {
                    try {
                        val value = deserializePayload<Payload<Offer>>(data.payload)

                        LoadResult.Page(
                            data = (value.objects.toList()),
                            /* no previous pagination int as page */
                            prevKey = page.takeIf { it > 0 }?.dec(),
                            /* no pagination if no results found else next page as +1 */
                            nextKey = page.takeIf { value.objects.size >= params.loadSize && value.isMore }
                                ?.inc()
                        )
                    } catch (e: Exception) {
                        throw ServerErrorException(
                            data.errorCode.toString(),
                            data.humanMessage.toString()
                        )
                    }
                } else {
                    throw ServerErrorException(
                        data.errorCode.toString(),
                        data.humanMessage.toString()
                    )
                }

            } catch (exception: ServerErrorException) {
                LoadResult.Error(exception)
            } catch (exception: Exception) {
                LoadResult.Error(exception)
            }
        }
}
