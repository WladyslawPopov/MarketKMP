package market.engine.fragments.root.main.profile.myOrders

import androidx.paging.map
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import market.engine.common.Platform
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.events.OrderItemEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.FilterBarUiState
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.ListingOfferContentState
import market.engine.core.data.states.MyOrderItemState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.DealType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import market.engine.core.repositories.OrderRepository
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class MyOrdersViewModel(
    private val orderSelected: Long?,
    val type: DealType,
    val component: MyOrdersComponent
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Order> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData(
        data = LD(
            filters = buildList {
                val filters = if (orderSelected != null){
                    DealFilters.getByTypeFilter(type).map {
                        if (it.key == "id")
                            it.copy(
                                value = orderSelected.toString(),
                                interpretation = "id: $orderSelected"
                            )
                        else it.copy()
                    }
                }else{
                    DealFilters.getByTypeFilter(type)
                }

                addAll(filters)
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
    ))
    private val _activeWindowType = MutableStateFlow(ActiveWindowListingType.LISTING)

    val pagingParamsFlow: Flow<ListingData> = combine(
        _listingData,
        updatePage
    ) { listingData, _ ->
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
                totalCount.update {
                    tc
                }
            }.map { pagingData ->
                pagingData.map { order ->
                    MyOrderItemState(
                        order = order,
                        orderRepository = OrderRepository(
                            order,
                            type,
                            this,
                            MyOrderItemEventsImpl(this, order, component)
                        )
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PagingData.empty()
        ).cachedIn(viewModelScope)

    val uiDataState: StateFlow<ListingOfferContentState> = combine(
        _activeWindowType,
        _listingData,
    )
    { activeType, listingData ->
        val ld = listingData.data
        val filterString = getString(strings.filter)
        val sortString = getString(strings.sort)
        val filters = ld.filters.filter { it.value != "" && it.interpretation?.isNotBlank() == true }

        ListingOfferContentState(
            appBarData = SimpleAppBarData(
                color = colors.primaryColor,
                onBackClick = {
                    onBack()
                },
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        onClick = { refresh() }
                    ),
                )
            ),
            listingData = listingData,
            filterBarData = FilterBarUiState(
                listFiltersButtons = buildList {
                    filters.forEach { filter ->
                        filter.interpretation?.let { text ->
                            add(
                                FilterListingBtnItem(
                                    text = text,
                                    itemClick = {
                                        _activeWindowType.value = ActiveWindowListingType.FILTERS
                                    },
                                    removeFilter = {
                                        removeFilter(filter)
                                    }
                                )
                            )
                        }
                    }
                    if (ld.sort != null) {
                        add(
                            FilterListingBtnItem(
                                text = sortString,
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowListingType.SORTING
                                },
                                removeFilter = {
                                    removeSort()
                                }
                            )
                        )
                    }
                },
                listNavigation = buildList {
                    add(
                        NavigationItem(
                            title = filterString,
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowListingType.FILTERS
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = ld.sort != null,
                            badgeCount = null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowListingType.SORTING
                            }
                        )
                    )
                }
            ),
            listingBaseState = ListingBaseState(
                listingData = listingData.data,
                searchData = listingData.searchData,
                activeWindowType = activeType,
                columns = if(isBigScreen.value) 2 else 1,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ListingOfferContentState()
    )

    fun updatePage(){
        updatePage.value++
        refresh()
    }

    fun onBack(){
        if(_activeWindowType.value != ActiveWindowListingType.LISTING){
            _activeWindowType.value = ActiveWindowListingType.LISTING
        }else{
            component.goToBack()
        }
    }

    fun applyFilters(newFilters: List<Filter>) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    filters = newFilters
                )
            )
        }
        refresh()
        _activeWindowType.value = ActiveWindowListingType.LISTING
    }
    fun applySorting(newSort: Sort?) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    sort = newSort
                )
            )
        }
        refresh()
        _activeWindowType.value = ActiveWindowListingType.LISTING
    }
    fun removeFilter(filter: Filter){
        _listingData.update { currentListingData ->
            val currentData = currentListingData.data
            val newFilters = currentData.filters.map { filterItem ->
                if (filterItem.key == filter.key && filterItem.operation == filter.operation) {
                    filterItem.copy(value = "", interpretation = null)
                } else {
                    filterItem
                }
            }
            currentListingData.copy(
                data = currentData.copy(filters = newFilters)
            )
        }
        refresh()
    }
    fun removeSort(){
        _listingData.update {
            it.copy(data = it.data.copy(sort = null))
        }
        refresh()
    }
    fun clearAllFilters() {
        DealFilters.clearTypeFilter(type)
        _listingData.update {
            it.copy(
                data = it.data.copy(filters = DealFilters.getByTypeFilter(type))
            )
        }
        refresh()
    }
}

data class MyOrderItemEventsImpl(
    val viewModel: MyOrdersViewModel,
    val order: Order,
    val component: MyOrdersComponent
) : OrderItemEvents {
    override fun onGoToUser(id: Long) {
        component.goToUser(id)
    }

    override fun onGoToOffer(offer: Offer) {
        component.goToOffer(offer)
    }

    override fun goToDialog(dialogId: Long?) {
        component.goToMessenger(dialogId)
    }
}
