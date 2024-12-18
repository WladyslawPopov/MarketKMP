package market.engine.core.network.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import market.engine.core.network.APIService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import market.engine.core.data.items.ListingData


class PagingRepository<T : Any>{
    private var genericPagingSource: GenericPagingSource<T>? = null

    fun getListing(listingData: ListingData, apiService: APIService, serializer: KSerializer<T>): Flow<PagingData<T>> = Pager(
        config = PagingConfig(
            pageSize = listingData.data.value.pageCountItems,
            initialLoadSize = listingData.data.value.pageCountItems,
            enablePlaceholders = false,
            prefetchDistance = listingData.data.value.pageCountItems * 3
        ),
        pagingSourceFactory = {
            GenericPagingSource(apiService, listingData, serializer).also {
                genericPagingSource = it
            }
        }
    ).flow

    fun refresh() {
        genericPagingSource?.invalidate()
    }
}
