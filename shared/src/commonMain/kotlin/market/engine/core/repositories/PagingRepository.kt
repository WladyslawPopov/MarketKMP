package market.engine.core.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import market.engine.core.network.APIService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import market.engine.core.data.constants.PAGES_MAX_SIZE
import market.engine.core.data.constants.PAGE_SIZE
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.network.paging.GenericPagingSource


class PagingRepository<T : Any>{
   fun getListing(
       ld: ListingData,
       apiService: APIService,
       serializer: KSerializer<T>,
       onTotalCountReceived: (Int) -> Unit = {}
   ): Flow<PagingData<T>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                maxSize = PAGES_MAX_SIZE,
                enablePlaceholders = false,
                prefetchDistance = PAGE_SIZE * 3

            ),
            pagingSourceFactory = {
                GenericPagingSource(apiService, ld, serializer, onTotalCountReceived)
            }
        ).flow
   }
}
