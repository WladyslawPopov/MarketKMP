package market.engine.core.network.paging.offer

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.APIService
import kotlinx.coroutines.flow.Flow
import market.engine.core.items.ListingData

class OfferPagingRepository(private val apiService: APIService) {
    private var offerPagingSource : OfferPagingSource? = null
    fun getListing(listingData: ListingData): Flow<PagingData<Offer>> = Pager(
        config = PagingConfig(
            pageSize = listingData.data.value.pageCountItems,
            initialLoadSize = listingData.data.value.pageCountItems,
            enablePlaceholders = false,
            prefetchDistance = listingData.data.value.pageCountItems*3
        ),
        pagingSourceFactory = {
            OfferPagingSource(apiService, listingData).also {
                offerPagingSource = it
            }
        }
    ).flow

    fun refresh(){
        offerPagingSource?.invalidate()
    }
}
