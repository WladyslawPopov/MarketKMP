package market.engine.business.core.network.paging.offer

import androidx.paging.PagingSource
import androidx.paging.PagingState
import application.market.auction_mobile.business.core.ServerErrorException
import application.market.auction_mobile.business.networkObjects.AppResponse
import application.market.auction_mobile.business.networkObjects.Offer
import application.market.auction_mobile.business.networkObjects.Payload
import application.market.auction_mobile.business.networkObjects.deserializePayload

open class OfferPagingSource(private val pagingData: suspend (page: Int, pageSize: Int) -> AppResponse) :
    PagingSource<Int, Offer>() {

    override fun getRefreshKey(state: PagingState<Int, Offer>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> =
        (params.key ?: 0).let { page ->
            try {
                pagingData(page, params.loadSize).let { data ->
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
                }
            } catch (exception: ServerErrorException) {
                LoadResult.Error(exception)
            } catch (exception: Exception) {
                LoadResult.Error(exception)
            }
        }
}
