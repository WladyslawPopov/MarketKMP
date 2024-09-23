package market.engine.business.core.network.paging.offer

import androidx.paging.PagingSource
import androidx.paging.PagingState
import market.engine.business.core.ServerErrorException
import application.market.agora.business.networkObjects.Offer
import application.market.agora.business.networkObjects.Payload
import application.market.agora.business.networkObjects.deserializePayload
import market.engine.business.core.UrlBuilder
import market.engine.business.core.network.APIService
import market.engine.business.items.ListingData

open class OfferPagingSource(private val apiService: APIService, private val listingData: ListingData?) :
    PagingSource<Int, Offer>() {

    override fun getRefreshKey(state: PagingState<Int, Offer>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> =
        (params.key ?: listingData?.data?.pgCount ?: 0).let { page ->
            try {
                val url = UrlBuilder()
                    .addPathSegment("offers")
                    .addPathSegment(listingData?.data?.methodServer ?: "")
                    .addQueryParameter("pg", page.toString())
                    .addQueryParameter("ipp", params.loadSize.toString())
                    .addFilters(listingData?.data,listingData?.searchData)
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
