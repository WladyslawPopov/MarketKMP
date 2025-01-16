package market.engine.fragments.profileMyOrders

import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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

    fun init(listingData : ListingData): Flow<PagingData<Order>> {
//        when(type){
//            LotsType.MYLOT_ACTIVE -> {
//                listingData.data.value.filters.clear()
//                listingData.data.value.filters.addAll( OfferFilters.filtersMyLotsActive.toList())
//            }
//            LotsType.MYLOT_UNACTIVE -> {
//                listingData.data.value.filters.clear()
//                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsUnactive.toList())
//            }
//            LotsType.MYLOT_FUTURE -> {
//                listingData.data.value.filters.clear()
//                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsFuture.toList())
//            }
//            else -> {
//                listingData.data.value.filters.clear()
//                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsActive.toList())
//            }
//        }

        listingData.data.value.methodServer = "get_cabinet_listing"
        listingData.data.value.objServer = "orders"

        return orderPagingRepository.getListing(listingData, apiService, Order.serializer()).cachedIn(viewModelScope)
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
