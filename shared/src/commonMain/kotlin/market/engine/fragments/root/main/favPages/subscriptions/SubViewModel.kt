package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class SubViewModel(
    val apiService: APIService,
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    val listingData = mutableStateOf(ListingData())


    fun init(): Flow<PagingData<Subscription>> {
        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "subscriptions"

        return pagingRepository.getListing(listingData.value, apiService, Subscription.serializer()).cachedIn(viewModelScope)
    }

    fun refresh(){
        pagingRepository.refresh()
    }
}
