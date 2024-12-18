package market.engine.core.network.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.serialization.KSerializer
import market.engine.core.data.items.ListingData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.UrlBuilder
import market.engine.core.network.APIService

open class GenericPagingSource<T : Any>(
    private val apiService: APIService,
    private val listingData: ListingData,
    private val serializer: KSerializer<T>
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition)
        return closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> =
        (params.key ?: 0).let { page ->
            try {
                val url = UrlBuilder()
                    .addPathSegment(listingData.data.value.objServer)
                    .addPathSegment(listingData.data.value.methodServer)
                    .addQueryParameter("pg", page.toString())
                    .addQueryParameter("ipp", params.loadSize.toString())
                    .addFilters(listingData.data.value, listingData.searchData.value)
                    .build()

                val data = apiService.getPage(url)

                if (data.success) {
                    try {
                        val serializer = Payload.serializer(serializer)
                        val value = deserializePayload(data.payload, serializer)
                        //val totalCount = value.totalCount
//                        val totalPages = if (totalCount % listingData.data.value.pageCountItems == 0) {
//                            totalCount / listingData.data.value.pageCountItems
//                        } else {
//                            (totalCount / listingData.data.value.pageCountItems) + 1
//                        }
                        listingData.data.value.totalCount = value.totalCount
//                        listingData.data.value.totalPages = totalPages

                        if (listingData.searchData.value.userSearch && listingData.searchData.value.userID != 1L && listingData.searchData.value.userLogin.isNullOrEmpty()){
                            val firstObject = value.objects.firstOrNull()
                            if (firstObject is Offer) {
                                listingData.searchData.value.userLogin = firstObject.sellerData?.login
                            }
                        }

                        LoadResult.Page(
                            data = value.objects.toList(),
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
