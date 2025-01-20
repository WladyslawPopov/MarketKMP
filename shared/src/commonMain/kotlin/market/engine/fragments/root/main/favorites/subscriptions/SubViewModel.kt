package market.engine.fragments.root.main.favorites.subscriptions

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class SubViewModel(
    apiService: APIService,
) : BaseViewModel() {
    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    var listingData = mutableStateOf(ListingData())
    val pagingDataFlow : Flow<PagingData<Subscription>>

    init {
        if (listingData.value.data.value.filters.isEmpty()) {
            listingData.value.data.value.filters.addAll(OfferFilters.filtersFav)
        }
        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "subscriptions"

        pagingDataFlow = pagingRepository.getListing(listingData.value, apiService, Subscription.serializer()).cachedIn(viewModelScope)
    }
}
