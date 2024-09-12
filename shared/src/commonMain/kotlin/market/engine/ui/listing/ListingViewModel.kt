package market.engine.ui.listing

import application.market.auction_mobile.business.networkObjects.Offer
import application.market.core.network.APIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.business.core.network.viewModels.BaseViewModel

class ListingViewModel(private val apiService: APIService) : BaseViewModel() {

    private val _responseOffers = MutableStateFlow<List<Offer>>(emptyList())
    val responseOffers: StateFlow<List<Offer>> = _responseOffers.asStateFlow()

    fun getPage(url : String){

    }
}
