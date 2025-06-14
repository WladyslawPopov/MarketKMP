package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.OrderFilterContent
import market.engine.widgets.filterContents.SortingOrdersContent
import market.engine.widgets.items.MyOrderItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOrdersContent(
    component: MyOrdersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val dealType = model.type
    val listingData = viewModel.listingData.value.data
    val searchData = viewModel.listingData.value.searchData
    val data = model.pagingDataFlow.collectAsLazyPagingItems()
    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val typeGroup = remember {  if (dealType in arrayOf(
            DealType.BUY_ARCHIVE,
            DealType.BUY_IN_WORK
        )
    ) DealTypeGroup.SELL else DealTypeGroup.BUY }

    val successToast = stringResource(strings.operationSuccess)

    val updateFilters = remember { mutableStateOf(0) }

    val refresh = remember{{
        viewModel.resetScroll()
        viewModel.onRefresh()
        data.refresh()
        updateFilters.value++
    }}

    BackHandler(model.backHandler){
//        when{
//            viewModel.activeFiltersType.value != "" ->{
//                viewModel.activeFiltersType.value = ""
//            }
//            else -> {
//                component.goToBack()
//            }
//        }
    }

    val noFound = @Composable {
        if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                DealFilters.clearTypeFilter(model.type)
                listingData.filters = DealFilters.getByTypeFilter(model.type)
                refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = if(typeGroup == DealTypeGroup.SELL) drawables.purchasesIcon else drawables.salesIcon
            ) {
                refresh()
            }
        }
    }

    val err = viewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err.value) { refresh() } }
    }else{
        null
    }

    //update item when we back
    LaunchedEffect(viewModel.updateItem.value) {
        if (viewModel.updateItem.value != null) {
            val oldOrder = data.itemSnapshotList.items.find { it.id == viewModel.updateItem.value }
            component.updateItem(oldOrder)
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            refresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
//        ListingBaseContent(
//            listingData = listingData.value,
//            searchData = searchData,
//            data = data,
//            baseViewModel = viewModel,
//            onRefresh = {
//                refresh()
//            },
//            noFound = noFound,
//            additionalBar = {
////                FiltersBar(
////                    searchData,
////                    listingData.value,
////                    updateFilters.value,
////                    isShowGrid = false,
////                    onFilterClick = {
////                        viewModel.activeFiltersType.value = "filters"
////                    },
////                    onSortClick = {
////                        viewModel.activeFiltersType.value = "sorting"
////                    },
////                    onRefresh = {
////                        refresh()
////                        updateFilters.value++
////                    }
////                )
//            },
//            filtersContent = { isRefreshingFromFilters, onClose ->
//                when(viewModel.activeFiltersType.value){
//                    "filters" -> {
//                        OrderFilterContent(
//                            isRefreshing = isRefreshingFromFilters,
//                            filters = listingData.value.filters,
//                            typeFilters = model.type,
//                            onClose = onClose
//                        )
//                    }
//                    "sorting" -> {
//                        SortingOrdersContent(
//                            isRefreshing = isRefreshingFromFilters,
//                            listingData.value,
//                            onClose = onClose
//                        )
//                    }
//                }
//            },
//            item = { order ->
//                AnimatedVisibility(order.owner != 1L, enter = fadeIn(), exit = fadeOut()) {
//                    MyOrderItem(
//                        order = order,
//                        typeGroup = typeGroup,
//                        goToUser = { id ->
//                            component.goToUser(id)
//                        },
//                        goToOffer = { offer ->
//                            component.goToOffer(offer)
//                        },
//                        onUpdateItem = {
//                            viewModel.updateItem.value = order.id
//
//                            viewModel.showToast(
//                                successToastItem.copy(
//                                    message = successToast
//                                )
//                            )
//                        },
//                        baseViewModel = viewModel,
//                        trigger = viewModel.updateItemTrigger.value,
//                        goToMessenger = { dialogId ->
//                            component.goToMessenger(dialogId)
//                        }
//                    )
//                }
//            }
//        )
    }
}
