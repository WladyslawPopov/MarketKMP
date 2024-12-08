package market.engine.presentation.profileMyOffers

import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.paging.PagingRepository
import market.engine.core.types.LotsType
import market.engine.presentation.base.BaseViewModel

class ProfileMyOffersViewModel(
    val type: LotsType,
    val apiService: APIService,
) : BaseViewModel() {
    private val offerPagingRepository: PagingRepository<Offer> = PagingRepository()
    var listingData = ListingData()
    val pagingDataFlow: Flow<PagingData<Offer>>

    init {

        when(type){
            LotsType.MYLOT_ACTIVE -> {
                listingData.data.value.filters = arrayListOf()
                listingData.data.value.filters.addAll( OfferFilters.filtersMyLotsActive.toList())
            }
            LotsType.MYLOT_UNACTIVE -> {
                listingData.data.value.filters = arrayListOf()
                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsUnactive.toList())
            }
            LotsType.MYLOT_FUTURE -> {
                listingData.data.value.filters = arrayListOf()
                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsFuture.toList())
            }
            else -> {
                listingData.data.value.filters = arrayListOf()
                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsActive.toList())
            }
        }

        listingData.data.value.methodServer = "get_cabinet_listing"
        listingData.data.value.objServer = "offers"

        pagingDataFlow = offerPagingRepository.getListing(listingData, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){
        offerPagingRepository.refresh()
    }
}
