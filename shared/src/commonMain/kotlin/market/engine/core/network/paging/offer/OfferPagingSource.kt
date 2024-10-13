package market.engine.core.network.paging.offer

import androidx.paging.PagingSource
import androidx.paging.PagingState
import market.engine.core.items.ListingData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.UrlBuilder
import market.engine.core.network.APIService

open class OfferPagingSource(private val apiService: APIService, private val listingData: ListingData) :
    PagingSource<Int, Offer>() {

    override fun getRefreshKey(state: PagingState<Int, Offer>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> =
        (params.key ?: 0 ).let { page ->
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
                        val totalCount = value.totalCount
                        val totalPages = if (totalCount % listingData.data.value.pageCountItems == 0) {
                            totalCount / listingData.data.value.pageCountItems
                        } else {
                            (totalCount / listingData.data.value.pageCountItems) + 1
                        }
                        listingData.data.value.totalCount = value.totalCount
                        listingData.data.value.totalPages = totalPages

                        LoadResult.Page(
                            data = (value.objects.toList()),
                            /* no previous pagination int as page */
                            prevKey = if (page > 0) page - 1 else null,
                            nextKey = if (value.isMore) page + 1 else null
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
