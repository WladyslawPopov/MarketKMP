package market.engine.fragments.root.main.profile.myOrders

import androidx.lifecycle.SavedStateHandle
import androidx.paging.map
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.events.OrderItemEvents
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.MyOrderItemState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import market.engine.core.repositories.OrderRapository
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.getMainTread
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import kotlin.collections.contains

class MyOrdersViewModel(
    private val orderSelected: Long?,
    val type: DealType,
    val component: MyOrdersComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {
    private val pagingRepository: PagingRepository<Order> = PagingRepository()

    val listingBaseViewModel = component.additionalModels.value.listingBaseViewModel
    private val ld = listingBaseViewModel.listingData
    private val activeType = listingBaseViewModel.activeWindowType

    val typeGroup = if (type in arrayOf(
            DealType.BUY_ARCHIVE,
            DealType.BUY_IN_WORK
        )
    ) DealTypeGroup.SELL else DealTypeGroup.BUY

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<MyOrderItemState>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Order.serializer()
            ){ tc ->
                listingBaseViewModel.setTotalCount(tc)
            }.map { pagingData ->
                pagingData.map { order ->
                    MyOrderItemState(
                        order = order,
                        orderRapository = OrderRapository(
                            order,
                            type,
                            MyOrderItemEventsImpl(this, order, component),
                            this,
                            savedStateHandle
                        )
                    )
                }
            }
        }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            listingBaseViewModel.setListingData(
                listingBaseViewModel.listingData.value.copy(
                    data = LD(
                        filters = if (orderSelected != null) {
                            DealFilters.getByTypeFilter(type).map {
                                if (it.key == "id")
                                    it.copy(
                                        value = orderSelected.toString(),
                                        interpretation = "id: $orderSelected"
                                    )
                                else it.copy()
                            }
                        } else {
                            DealFilters.getByTypeFilter(type)
                        },
                        methodServer = buildString {
                            val method = if (type in arrayOf(
                                    DealType.BUY_ARCHIVE,
                                    DealType.BUY_IN_WORK
                                )
                            ) "purchases" else "sales"

                            append("get_cabinet_listing_$method")
                        },
                        objServer = "orders"
                    ),
                )
            )

            listingBaseViewModel.setListItemsFilterBar(
                buildList {
                    val filterString = getString(strings.filter)
                    val sortString = getString(strings.sort)
                    val filters = ld.value.data.filters.filter {
                        it.value != "" &&
                                it.interpretation?.isNotBlank() == true
                    }

                    add(
                        NavigationItem(
                            title = filterString,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            hasNews = ld.value.data.sort != null,
                            badgeCount = null,
                        )
                    )
                }
            )

            val eventParameters = mapOf(
                "user_id" to UserData.login.toString(),
                "profile_source" to "deals",
                "type" to typeGroup.name
            )
            analyticsHelper.reportEvent("view_seller_profile", eventParameters)
        }
    }

    fun onBack(goBack : () -> Unit){
        if(activeType.value != ActiveWindowListingType.LISTING){
            listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
        }else{
            goBack()
        }
    }
}

data class MyOrderItemEventsImpl(
    val viewModel: MyOrdersViewModel,
    val order: Order,
    val component: MyOrdersComponent
) : OrderItemEvents {
    override fun onGoToUser(id: Long) {
        viewModel.getMainTread {
            component.goToUser(id)
        }
    }

    override fun onGoToOffer(offer: Offer) {
        viewModel.getMainTread {
            component.goToOffer(offer)
        }
    }

    override fun goToDialog(dialogId: Long?) {
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.Main) {
                component.goToMessenger(dialogId)
            }
        }
    }
}
