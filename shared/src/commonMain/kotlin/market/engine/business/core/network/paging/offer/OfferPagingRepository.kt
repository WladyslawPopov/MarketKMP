package market.engine.business.core.network.paging.offer

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import market.engine.business.items.ListingData
import application.market.agora.business.networkObjects.Offer
import market.engine.business.core.network.APIService
import kotlinx.coroutines.flow.Flow

class OfferPagingRepository(private val apiService: APIService) {
    fun getListing(listingData: ListingData?): Flow<PagingData<Offer>> = Pager(
        config = PagingConfig(
            pageSize = listingData?.data?.pageCountItems ?: 30,
            initialLoadSize = (listingData?.data?.pageCountItems?:30)*3, enablePlaceholders = false
        ),
        pagingSourceFactory = {
            OfferPagingSource(apiService, listingData)
        }
    ).flow
}
