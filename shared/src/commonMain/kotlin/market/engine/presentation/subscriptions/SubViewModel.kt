package market.engine.presentation.subscriptions

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.network.paging.PagingRepository
import market.engine.presentation.base.BaseViewModel

class SubViewModel(
    private val apiService: APIService,
) : BaseViewModel() {
    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    var listingData = mutableStateOf(ListingData())

    val pagingDataFlow : Flow<PagingData<Subscription>>

    init {
        if (listingData.value.data.value.filters.isNullOrEmpty()) {
            listingData.value.data.value.filters = arrayListOf()
            listingData.value.data.value.filters?.addAll(OfferFilters.filtersFav)
        }
        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "subscriptions"

        pagingDataFlow = pagingRepository.getListing(listingData.value, apiService, Subscription.serializer()).cachedIn(viewModelScope)
    }
}
