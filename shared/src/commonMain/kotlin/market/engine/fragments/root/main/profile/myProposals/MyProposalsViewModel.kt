package market.engine.fragments.root.main.profile.myProposals

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.LotsType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel

class MyProposalsViewModel(
    val type: LotsType,
) : BaseViewModel() {

    private val offerPagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<OfferItem>> {
        listingData.value.data.filters = OfferFilters.getByTypeFilter(type)
        listingData.value.data.methodServer = "get_cabinet_listing_my_price_proposals"
        listingData.value.data.objServer = "offers"

        return offerPagingRepository.getListing(listingData.value, apiService, Offer.serializer()).map {
            it.map { offer ->
                offer.parseToOfferItem()
            }
        }.cachedIn(viewModelScope)
    }

    fun onRefresh(){
    }
}
