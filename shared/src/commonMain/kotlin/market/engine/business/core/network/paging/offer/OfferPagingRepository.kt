package market.engine.business.core.network.paging.offer

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import market.engine.business.items.ListingData
import application.market.auction_mobile.business.networkObjects.Offer
import application.market.core.network.APIService
import kotlinx.coroutines.flow.Flow
import market.engine.business.core.UrlBuilder

class OfferPagingRepository(private val apiService: APIService) {
    fun getListing(listingData: ListingData?): Flow<PagingData<Offer>> = Pager(
        config = PagingConfig(pageSize = listingData?.data?.pageCountItems ?: 30, initialLoadSize = (listingData?.data?.pageCountItems?:30)*3, enablePlaceholders = false),
        pagingSourceFactory = {
            OfferPagingSource { page, size ->

                val url = UrlBuilder()
                    .addPathSegment("offers")
                    .addPathSegment(listingData?.data?.methodServer ?: "")
                    .addQueryParameter("pg", page.toString())
                    .addQueryParameter("ipp", size.toString())
                    .addFilters(listingData?.data,listingData?.searchData)
                    .build()

               apiService.getPageOffers(url)
            }
        }
    ).flow
}
