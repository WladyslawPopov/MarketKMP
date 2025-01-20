package market.engine.fragments.root.main.favorites

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class FavViewModel(
    private val apiService: APIService,
    private val userRepository : UserRepository
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Offer>> {
        if (listingData.value.data.value.filters.isEmpty()) {
            listingData.value.data.value.filters.clear()
            listingData.value.data.value.filters.addAll(OfferFilters.filtersFav)
        }
        listingData.value.data.value.methodServer = "get_cabinet_listing_watched_by_me"
        listingData.value.data.value.objServer = "offers"

        return pagingRepository.getListing(listingData.value, apiService, Offer.serializer()).cachedIn(viewModelScope)
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
