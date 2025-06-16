package market.engine.core.network.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.serialization.KSerializer
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.UrlBuilder
import market.engine.core.network.APIService
import market.engine.core.utils.deserializePayload


open class GenericPagingSource<T : Any>(
    private val apiService: APIService,
    private val listingData: ListingData,
    private val serializer: KSerializer<T>,
    private val onTotalCountReceived: (Int) -> Unit
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> =
        (params.key ?: 0).let { page ->
            try {
                val url = UrlBuilder()
                    .addPathSegment(listingData.data.objServer)
                    .addPathSegment(listingData.data.methodServer)
                    .addQueryParameter("pg", page.toString())
                    .addQueryParameter("ipp", params.loadSize.toString())
                    .addFilters(listingData.data, listingData.searchData)
                    .build()

                val data = apiService.getPage(url)

                if (data.success) {
                    try {
                        val serializer = Payload.serializer(serializer)
                        val value = deserializePayload(data.payload, serializer)
                        onTotalCountReceived(value.totalCount)

                        LoadResult.Page(
                            data = value.objects.toList(),
                            prevKey = if (page > 0) page - 1 else null,
                            nextKey = if (value.isMore) page + 1 else null
                        )

                    } catch (_: Exception) {
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
