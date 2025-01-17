package market.engine.fragments.root.main.favorites

import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.paging.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class FavViewModel(
    private val apiService: APIService,
    private val userRepository : UserRepository
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    fun init(listingData: ListingData): Flow<PagingData<Offer>> {
        if (listingData.data.value.filters.isEmpty()) {
            listingData.data.value.filters.clear()
            listingData.data.value.filters.addAll(OfferFilters.filtersFav)
        }
        listingData.data.value.methodServer = "get_cabinet_listing_watched_by_me"
        listingData.data.value.objServer = "offers"

        return pagingRepository.getListing(listingData, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun refresh(){
        pagingRepository.refresh()
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            userRepository.updateUserInfo()
        }
    }
}
