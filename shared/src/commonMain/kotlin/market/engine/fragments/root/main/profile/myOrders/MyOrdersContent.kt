package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.WindowType
import market.engine.core.network.functions.OrderOperations
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.OrderFilterContent
import market.engine.widgets.filterContents.SortingOrdersContent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MyOrdersContent(
    component: MyOrdersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val dealType = model.type
    val listingData = model.listingData.data
    val searchData = model.listingData.searchData
    val data = model.pagingDataFlow.collectAsLazyPagingItems()
    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val orderOperations : OrderOperations = koinInject()

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val successToast = stringResource(strings.operationSuccess)

    val refresh = {
        listingData.value.resetScroll()
        viewModel.onRefresh()
    }

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpritation != null && it.interpritation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                listingData.value.clearFilters()
                listingData.value.resetScroll()
                viewModel.onRefresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = drawables.emptyOffersIcon
            ) {
                listingData.value.resetScroll()
                viewModel.onRefresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(viewModel.updateItem.value) {
        if (viewModel.updateItem.value != null) {
            val res = withContext(Dispatchers.Default) {
                orderOperations.getOrder(viewModel.updateItem.value!!)
            }
            val buf = res.success
            val err = res.error
            withContext(Dispatchers.Main) {
                if (buf != null) {
                    val oldOrder = data.itemSnapshotList.items.find { it.id == buf.id }
                    oldOrder?.trackId = buf.trackId
                    oldOrder?.marks = buf.marks
                    oldOrder?.feedbacks = buf.feedbacks
                    oldOrder?.comment = buf.comment
                    oldOrder?.paymentMethod = buf.paymentMethod
                    oldOrder?.deliveryMethod = buf.deliveryMethod
                    oldOrder?.deliveryAddress = buf.deliveryAddress
                    oldOrder?.dealType = buf.dealType
                    oldOrder?.lastUpdatedTs = buf.lastUpdatedTs
                }else if (err != null) {
                    viewModel.onError(err)
                }
                viewModel.updateItem.value = null
            }
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            refresh()
        },
        error = null,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            columns = columns,
            listingData = listingData.value,
            searchData = searchData.value,
            data = data,
            baseViewModel = viewModel,
            onRefresh = {
                refresh()
            },
            noFound = noFound,
            additionalBar = {
                FiltersBar(
                    searchData.value,
                    listingData.value,
                    isShowGrid = false,
                    onFilterClick = {
                        viewModel.activeFiltersType.value = "filters"
                    },
                    onSortClick = {
                        viewModel.activeFiltersType.value = "sorting"
                    },
                    onRefresh = {
                        refresh()
                    }
                )
            },
            filtersContent = { isRefreshingFromFilters, onClose ->
                when(viewModel.activeFiltersType.value){
                    "filters" -> {
                        OrderFilterContent(
                            isRefreshing = isRefreshingFromFilters,
                            filters = listingData.value.filters,
                            typeFilters = model.type,
                            onClose = onClose
                        )
                    }
                    "sorting" -> {
                        SortingOrdersContent(
                            isRefreshing = isRefreshingFromFilters,
                            listingData.value,
                            onClose = onClose
                        )
                    }
                }
            },
            item = { order ->
                MyOrderItem(
                    order = order,
                    typeGroup = if (dealType in arrayOf(
                            DealType.BUY_ARCHIVE,
                            DealType.BUY_IN_WORK
                        )
                    ) DealTypeGroup.SELL else DealTypeGroup.BUY,
                    goToUser = { id ->
                        component.goToUser(id)
                    },
                    goToOffer = { offer ->
                        component.goToOffer(offer)
                    },
                    onUpdateItem = {
                        viewModel.updateItem.value = order.id
                    },
                    baseViewModel = viewModel
                )
            }
        )
    }
}
