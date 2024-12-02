package market.engine.presentation.profileMyOffers

import androidx.compose.runtime.mutableStateOf
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
    var listingData = mutableStateOf(ListingData())
    val pagingDataFlow: Flow<PagingData<Offer>>

    init {

        when(type){
            LotsType.MYLOT_ACTIVE -> {
                listingData.value.data.value.filters = arrayListOf()
                listingData.value.data.value.filters?.addAll( OfferFilters.filtersMyLotsActive)
            }
            LotsType.MYLOT_UNACTIVE -> {
                listingData.value.data.value.filters = arrayListOf()
                listingData.value.data.value.filters?.addAll(OfferFilters.filtersMyLotsUnactive)
            }
            LotsType.MYLOT_FUTURE -> {
                listingData.value.data.value.filters = arrayListOf()
                listingData.value.data.value.filters?.addAll(OfferFilters.filtersMyLotsFuture)
            }
            else -> {
                listingData.value.data.value.filters = arrayListOf()
                listingData.value.data.value.filters?.addAll(OfferFilters.filtersMyLotsActive)
            }
        }

        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "offers"

        pagingDataFlow = offerPagingRepository.getListing(listingData.value, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }
}
