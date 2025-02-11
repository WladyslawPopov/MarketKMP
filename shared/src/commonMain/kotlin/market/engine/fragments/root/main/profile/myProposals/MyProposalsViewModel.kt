package market.engine.fragments.root.main.profile.myProposals

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.LotsType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class MyProposalsViewModel(
    val type: LotsType,
) : BaseViewModel() {

    private val offerPagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Offer>> {
        when(type){
            LotsType.ALL_PROPOSAL -> {
                listingData.value.data.value.filters = OfferFilters.filtersProposeAll
            }
            LotsType.NEED_RESPOSE -> {
                listingData.value.data.value.filters = OfferFilters.filtersProposeNeed
            }
            else -> {
                listingData.value.data.value.filters = OfferFilters.filtersProposeAll
            }
        }

        listingData.value.data.value.methodServer = "get_cabinet_listing_my_price_proposals"
        listingData.value.data.value.objServer = "offers"

        return offerPagingRepository.getListing(listingData.value, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){
        offerPagingRepository.refresh()
    }
}
