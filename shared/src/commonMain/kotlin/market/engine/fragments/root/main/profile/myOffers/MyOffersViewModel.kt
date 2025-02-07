package market.engine.fragments.root.main.profile.myOffers

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.LotsType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class MyOffersViewModel(
    val type: LotsType
) : BaseViewModel() {

    private val offerPagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Offer>> {
        when(type){
            LotsType.MYLOT_ACTIVE -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll( OfferFilters.filtersMyLotsActive.toList())
            }
            LotsType.MYLOT_UNACTIVE -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(OfferFilters.filtersMyLotsUnactive.toList())
            }
            LotsType.MYLOT_FUTURE -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(OfferFilters.filtersMyLotsFuture.toList())
            }
            else -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(OfferFilters.filtersMyLotsActive.toList())
            }
        }

        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "offers"

        return offerPagingRepository.getListing(listingData.value, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){
        offerPagingRepository.refresh()
    }
}
