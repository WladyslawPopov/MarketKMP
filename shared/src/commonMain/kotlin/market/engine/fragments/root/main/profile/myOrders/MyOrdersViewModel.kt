package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.DealType
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.UrlBuilder
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class MyOrdersViewModel(
    private val orderSelected: Long?,
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
        if (orderSelected != null) {
            listingData.value.data.value.filters.find { it.key == "id" }?.value = orderSelected.toString()
            listingData.value.data.value.filters.find { it.key == "id" }?.interpritation = "id: $orderSelected"
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

    suspend fun updateItem(id : Long?) : Order? {
        try {
            val ld = ListingData()

            when (type) {
                DealType.BUY_IN_WORK -> {
                    ld.data.value.filters.clear()
                    ld.data.value.filters.addAll(DealFilters.filtersBuysInWork.toMutableList())
                }

                DealType.BUY_ARCHIVE -> {
                    ld.data.value.filters.clear()
                    ld.data.value.filters.addAll(DealFilters.filtersBuysArchive.toMutableList())
                }

                DealType.SELL_ALL -> {
                    ld.data.value.filters.clear()
                    ld.data.value.filters.addAll(DealFilters.filtersSalesAll.toMutableList())
                }

                DealType.SELL_IN_WORK -> {
                    ld.data.value.filters.clear()
                    ld.data.value.filters.addAll(DealFilters.filtersSalesInWork.toMutableList())
                }

                DealType.SELL_ARCHIVE -> {
                    ld.data.value.filters.clear()
                    ld.data.value.filters.addAll(DealFilters.filtersSalesArchive.toMutableList())
                }
            }

            val method = if (type in arrayOf(
                    DealType.BUY_ARCHIVE,
                    DealType.BUY_IN_WORK
                )
            ) "purchases" else "sales"
            ld.data.value.objServer = "orders"

            ld.data.value.methodServer = "get_cabinet_listing_$method"

            ld.data.value.filters.find { it.key == "id" }?.value = id.toString()
            ld.data.value.filters.find { it.key == "id" }?.interpritation = ""

            val url = UrlBuilder()
                .addPathSegment(ld.data.value.objServer)
                .addPathSegment(ld.data.value.methodServer)
                .addFilters(ld.data.value, ld.searchData.value)
                .build()

            val res = withContext(Dispatchers.Default) {
                apiService.getPage(url)
            }
            return withContext(Dispatchers.Main) {
                ld.data.value.filters.find { it.key == "id" }?.value = ""
                ld.data.value.filters.find { it.key == "id" }?.interpritation = null

                if (res.success) {
                    val serializer = Payload.serializer(Order.serializer())
                    val payload = deserializePayload(res.payload, serializer)
                    return@withContext payload.objects.firstOrNull()
                } else {
                    return@withContext null
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            return null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            return null
        }
    }
}
