package market.engine.ui.listing

import androidx.paging.PagingData
import market.engine.business.items.ListingData
import application.market.auction_mobile.business.networkObjects.Offer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.business.core.network.paging.offer.OfferPagingRepository
import market.engine.business.core.network.viewModels.BaseViewModel

class ListingViewModel(private val offerPagingRepository: OfferPagingRepository) : BaseViewModel() {

    private val _responseOffers = MutableStateFlow<List<Offer>>(emptyList())
    val responseOffers: StateFlow<List<Offer>> = _responseOffers.asStateFlow()

    fun getPage(listingData: ListingData) : Flow<PagingData<Offer>> {
        return offerPagingRepository.getListing(listingData)
    }
}
