package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.OnError
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

    val uiState = viewModel.uiDataState.collectAsState()
    val listingData = uiState.value.listingData.data
    val activeWindowType = uiState.value.listingBaseState.activeWindowType
    val listingBaseState = uiState.value.listingBaseState
    val filterBarUiState = uiState.value.filterBarData

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem = viewModel.updateItem.collectAsState()


    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val dealType = model.type

    val typeGroup = remember {  if (dealType in arrayOf(
            DealType.BUY_ARCHIVE,
            DealType.BUY_IN_WORK
        )
    ) DealTypeGroup.SELL else DealTypeGroup.BUY }

    BackHandler(model.backHandler){
        viewModel.onBack()
    }

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        viewModel.clearAllFilters()
                        viewModel.updatePage()
                    }
                } else {
                    showNoItemLayout(
                        title = stringResource(strings.simpleNotFoundLabel),
                        icon = if (typeGroup == DealTypeGroup.SELL) drawables.purchasesIcon else drawables.salesIcon
                    ) {
                        viewModel.updatePage()
                    }
                }
            }
        } else {
            null
        }
    }

    val err = viewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            {
                OnError(err.value) {
                    viewModel.updatePage()
                }
            }
        } else {
            null
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            viewModel.updatePage()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            uiState = listingBaseState,
            data = data,
            baseViewModel = viewModel,
            noFound = noFound,
            additionalBar = {
                FiltersBar(filterBarUiState)
            },
            filtersContent = {
                when (activeWindowType) {
                    ActiveWindowListingType.FILTERS -> {
                        OrderFilterContent(
                            initialFilters = listingData.filters,
                            typeFilters = model.type,
                        ){ newFilters ->
                            viewModel.applyFilters(newFilters)
                        }
                    }

                    ActiveWindowListingType.SORTING -> {
                        SortingOrdersContent(
                            listingData.sort,
                        ){ newSort ->
                            viewModel.applySorting(newSort)
                        }
                    }
                    else -> {

                    }
                }
            },
            item = { order ->
                MyOrderItem(
                    order,
                    updateItem.value
                )
            }
        )
    }
}
