package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.ui.text.AnnotatedString
import androidx.paging.map
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.Platform
import market.engine.common.clipBoardEvent
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
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.states.CategoryState
import market.engine.core.data.states.FilterBarUiState
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.ListingOfferContentState
import market.engine.core.data.states.MyOrderItemState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.UrlBuilder
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel
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
                    val f = DealFilters.getByTypeFilter(type)
                    f.find { it.key == "id" }?.copy(
                        interpretation = "id: $orderSelected",
                        value = orderSelected.toString()
                    )
                    f
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
    val showOperationsDialog = MutableStateFlow("")
    val titleDialog = MutableStateFlow(AnnotatedString(""))
    val dialogItemId = MutableStateFlow(1L)

    val showMessageDialog = MutableStateFlow(false)
    val showDetailsDialog = MutableStateFlow(false)

    val pagingParamsFlow: Flow<ListingData> = combine(
        _listingData,
        updatePage
    ) { listingData, _ ->
        listingData
    }

    private val filtersCategoryModel = CategoryViewModel(
        isFilters = true,
    )

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
                    val typeGroup = if (type in arrayOf(
                            DealType.BUY_ARCHIVE,
                            DealType.BUY_IN_WORK
                        )
                    ) DealTypeGroup.SELL else DealTypeGroup.BUY

                    MyOrderItemState(
                        order = order,
                        typeGroup = typeGroup,
                        events = MyOrderItemEventsImpl(this, order, component)
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
    ) { activeType, listingData ->
        val ld = listingData.data
        val filterString = getString(strings.filter)
        val sortString = getString(strings.sort)
        val filters = ld.filters.filter { it.value != "" && it.interpretation?.isNotBlank() == true }

        filtersCategoryModel.updateFromSearchData(listingData.searchData)
        filtersCategoryModel.initialize(listingData.data.filters)

        ListingOfferContentState(
            appBarData = SimpleAppBarData(
                color = colors.primaryColor,
                onBackClick = {
                    onBackNavigation(activeType)
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
            filtersCategoryState = CategoryState(
                openCategory = activeType == ActiveWindowListingType.CATEGORY,
                categoryViewModel = filtersCategoryModel
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

    fun onBackNavigation(activeType: ActiveWindowListingType) {
        when(activeType){
            ActiveWindowListingType.CATEGORY_FILTERS -> {
                if (filtersCategoryModel.categoryId.value != 1L){
                    filtersCategoryModel.navigateBack()
                }else{
                    _activeWindowType.value = ActiveWindowListingType.LISTING
                }
            }
            else -> {
                _activeWindowType.value = ActiveWindowListingType.LISTING
            }
        }
    }

    private suspend fun getItem(id : Long): Order? {
        return try {
            val ld = ListingData()
            ld.data.filters = DealFilters.getByTypeFilter(type)

            val method = if (type in arrayOf(
                    DealType.BUY_ARCHIVE,
                    DealType.BUY_IN_WORK
                )
            ) "purchases" else "sales"
            ld.data.objServer = "orders"

            ld.data.methodServer = "get_cabinet_listing_$method"

            ld.data.filters.find { it.key == "id" }?.value = id.toString()
            ld.data.filters.find { it.key == "id" }?.interpretation = ""

            val url = UrlBuilder()
                .addPathSegment(ld.data.objServer)
                .addPathSegment(ld.data.methodServer)
                .addFilters(ld.data, ld.searchData)
                .build()

            val res = withContext(Dispatchers.IO) {
                apiService.getPage(url)
            }
           return withContext(Dispatchers.Main) {
                ld.data.filters.find { it.key == "id" }?.value = ""
                ld.data.filters.find { it.key == "id" }?.interpretation = null

                if (res.success) {
                    val serializer = Payload.serializer(Order.serializer())
                    val payload = deserializePayload(res.payload, serializer)


                    return@withContext payload.objects.firstOrNull()
                }else{
                    return@withContext null
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            null
        }
    }

    fun updateItem(oldOrder: Order) {
        viewModelScope.launch {
            val buf = withContext(Dispatchers.IO) {
                getItem(oldOrder.id)
            }

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    oldOrder.owner = buf.owner
                    oldOrder.trackId = buf.trackId
                    oldOrder.marks = buf.marks
                    oldOrder.feedbacks = buf.feedbacks
                    oldOrder.comment = buf.comment
                    oldOrder.paymentMethod = buf.paymentMethod
                    oldOrder.deliveryMethod = buf.deliveryMethod
                    oldOrder.deliveryAddress = buf.deliveryAddress
                    oldOrder.dealType = buf.dealType
                    oldOrder.lastUpdatedTs = buf.lastUpdatedTs
                }else {
                    oldOrder.owner = 1L
                }
                updateItem.value = null
            }
        }
    }

    fun getOperations(order: Order, onGetOperations: (List<MenuItem>) -> Unit) {
        viewModelScope.launch {
            getOrderOperations(order.id) { listOperations ->
                onGetOperations(
                    buildList {
                        addAll(listOperations.map { operation ->
                            MenuItem(
                                id = operation.id ?: "",
                                title = operation.name ?: "",
                                onClick = {
                                    operation.run {
                                        when (id) {
                                            "give_feedback_to_seller" -> {
                                                titleDialog.value = AnnotatedString(name ?: "")
                                                showOperationsDialog.value = "give_feedback_to_seller"
                                            }

                                            "give_feedback_to_buyer" -> {
                                                titleDialog.value = AnnotatedString(name ?: "")
                                                showOperationsDialog.value = "give_feedback_to_buyer"
                                            }

                                            "set_comment" -> {
                                                titleDialog.value = AnnotatedString(name ?: "")
                                                showOperationsDialog.value = "set_comment"
                                            }

                                            "provide_track_id" -> {
                                                titleDialog.value = AnnotatedString(name ?: "")
                                                showOperationsDialog.value = "provide_track_id"
                                            }

                                            else -> {
                                                titleDialog.value = AnnotatedString(name ?: "")
                                                postOperationFields(
                                                    order.id,
                                                    id ?: "",
                                                    "orders",
                                                    onSuccess = {
                                                        val eventParameters = mapOf(
                                                            "order_id" to order.id,
                                                            "seller_id" to order.sellerData?.id,
                                                            "buyer_id" to order.buyerData?.id
                                                        )

                                                        analyticsHelper.reportEvent(
                                                            operation.id ?: "",
                                                            eventParameters
                                                        )

                                                        updateItem.value = order.id
                                                    },
                                                    errorCallback = {

                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        })
                    }
                )
            }
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
    fun clearDialogFields(){
        dialogItemId.value = 1
        showOperationsDialog.value = ""
        titleDialog.value = AnnotatedString("")
        showMessageDialog.value = false
        showDetailsDialog.value = false
    }
}

data class MyOrderItemEventsImpl(
    val viewModel: MyOrdersViewModel,
    val order: Order,
    val component: MyOrdersComponent
) : OrderItemEvents {
    override fun onUpdateItem() {
        viewModel.updateItem(order)
    }

    override fun onGoToUser(id: Long) {
        component.goToUser(id)
    }

    override fun onGoToOffer(offer: Offer) {
        component.goToOffer(offer)
    }

    override fun sendMessage() {
        viewModel.showMessageDialog.value = true
        viewModel.dialogItemId.value = order.id
    }

    override fun openOrderDetails() {
        viewModel.showDetailsDialog.value = true
        viewModel.dialogItemId.value = order.id
    }

    override fun getOperations(onGetOperations: (List<MenuItem>) -> Unit) {
        viewModel.getOperations(order) {
            onGetOperations(it)
        }
    }

    override fun copyTrackId() {
        viewModel.viewModelScope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.trackId.toString())

            viewModel.showToast(
                ToastItem(
                    isVisible = true,
                    message = idString,
                    type = ToastType.SUCCESS
                )
            )
        }
    }

    override fun copyOrderId() {
        viewModel.viewModelScope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.id.toString())

            viewModel.showToast(
                ToastItem(
                    isVisible = true,
                    message = idString,
                    type = ToastType.SUCCESS
                )
            )
        }
    }
}
