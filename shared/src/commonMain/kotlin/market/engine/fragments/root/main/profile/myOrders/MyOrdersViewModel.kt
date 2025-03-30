package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.DealType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.UrlBuilder
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class MyOrdersViewModel(
    private val orderSelected: Long?,
    val type: DealType
) : BaseViewModel() {

    private val orderPagingRepository: PagingRepository<Order> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Order>> {
        listingData.value.data.value.filters = DealFilters.getByTypeFilter(type)

        if (orderSelected != null) {
            listingData.value.data.value.filters.find { it.key == "id" }?.value = orderSelected.toString()
            listingData.value.data.value.filters.find { it.key == "id" }?.interpretation = "id: $orderSelected"
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

    suspend fun updateItem(id : Long?) : Order? {
        try {
            val ld = ListingData()
            ld.data.value.filters = DealFilters.getByTypeFilter(type)

            val method = if (type in arrayOf(
                    DealType.BUY_ARCHIVE,
                    DealType.BUY_IN_WORK
                )
            ) "purchases" else "sales"
            ld.data.value.objServer = "orders"

            ld.data.value.methodServer = "get_cabinet_listing_$method"

            ld.data.value.filters.find { it.key == "id" }?.value = id.toString()
            ld.data.value.filters.find { it.key == "id" }?.interpretation = ""

            val url = UrlBuilder()
                .addPathSegment(ld.data.value.objServer)
                .addPathSegment(ld.data.value.methodServer)
                .addFilters(ld.data.value, ld.searchData.value)
                .build()

            val res = withContext(Dispatchers.IO) {
                apiService.getPage(url)
            }
            return withContext(Dispatchers.Main) {
                ld.data.value.filters.find { it.key == "id" }?.value = ""
                ld.data.value.filters.find { it.key == "id" }?.interpretation = null

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
