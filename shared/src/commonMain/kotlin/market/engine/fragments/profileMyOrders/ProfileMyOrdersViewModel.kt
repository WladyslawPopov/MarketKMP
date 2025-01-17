package market.engine.fragments.profileMyOrders

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.DealType
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.paging.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class ProfileMyOrdersViewModel(
    val type: DealType,
    val apiService: APIService,
    val userRepository: UserRepository
) : BaseViewModel() {

    private val orderPagingRepository: PagingRepository<Order> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Order>> {

        when (type) {
            DealType.BUY_IN_WORK -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(DealFilters.filtersBuysInWork.toList())
            }

            DealType.BUY_ARCHIVE -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(DealFilters.filtersBuysArchive)
            }

            DealType.SELL_ALL -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(DealFilters.filtersSalesAll)
            }

            DealType.SELL_IN_WORK -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(DealFilters.filtersSalesInWork)
            }

            DealType.SELL_ARCHIVE -> {
                listingData.value.data.value.filters.clear()
                listingData.value.data.value.filters.addAll(DealFilters.filtersSalesArchive)
            }
        }

        val method = if (type in arrayOf(
                DealType.BUY_ARCHIVE,
                DealType.BUY_IN_WORK
            )
        ) "purchases" else "sales"
        listingData.value.data.value.objServer = "orders"

        listingData.value.data.value.methodServer = "get_cabinet_listing_$method"

        return orderPagingRepository.getListing(listingData.value, apiService, Order.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){
        orderPagingRepository.refresh()
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            userRepository.updateToken()
            userRepository.updateUserInfo()
        }
    }
}
