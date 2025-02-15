package market.engine.fragments.root.main.favPages.favorites

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.LotsType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class FavViewModel : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Offer>> {
        listingData.value.data.value.filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES)
        listingData.value.data.value.methodServer = "get_cabinet_listing_watched_by_me"
        listingData.value.data.value.objServer = "offers"

        return pagingRepository.getListing(listingData.value, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun refresh(){
        updateUserInfo()
        pagingRepository.refresh()
    }
}
