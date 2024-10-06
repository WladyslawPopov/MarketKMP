package market.engine.core.network.paging.offer

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import market.engine.core.items.ListingData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.APIService
import kotlinx.coroutines.flow.Flow

class OfferPagingRepository(private val apiService: APIService) {
    fun getListing(listingData: ListingData): Flow<PagingData<Offer>> = Pager(
        config = PagingConfig(
            pageSize = listingData.data.value.pageCountItems,
            initialLoadSize = (listingData.data.value.pageCountItems)*3, enablePlaceholders = false
        ),
        pagingSourceFactory = {
            OfferPagingSource(apiService, listingData)
        }
    ).flow
}
