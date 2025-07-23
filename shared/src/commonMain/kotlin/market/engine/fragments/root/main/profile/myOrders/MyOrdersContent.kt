package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.listing.listingNotFoundView
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.filterContents.OrderFilterContent
import market.engine.widgets.filterContents.SortingOrdersContent
import market.engine.widgets.items.MyOrderItem

@Composable
fun MyOrdersContent(
    component: MyOrdersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel

    val listingBaseViewModel = viewModel.listingBaseViewModel
    val listingDataState by listingBaseViewModel.listingData.collectAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem by viewModel.updateItem.collectAsState()

    val activeType by listingBaseViewModel.activeWindowType.collectAsState()

    val listingData = listingDataState.data

    val toastItem by viewModel.toastItem.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    BackHandler(model.backHandler){
        viewModel.onBack()
    }

    val listingState = rememberLazyScrollState(viewModel)

    val hasActiveFilters by remember(listingDataState) {
        mutableStateOf(
            listingData.filters.any { it.interpretation?.isNotBlank() == true }
        )
    }

    val noFound = listingNotFoundView(
        isLoading = data.loadState.refresh is LoadStateNotLoading,
        itemCount = data.itemCount,
        activeType = activeType,
        hasActiveFilters = hasActiveFilters,
        onClearFilters = listingBaseViewModel::clearListingData,
        onRefresh = listingBaseViewModel::refresh
    )

    val err = viewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            {
                OnError(err.value) {
                    viewModel.refresh()
                }
            }
        } else {
            null
        }
    }

    when(activeType){
        ActiveWindowListingType.FILTERS -> {
            OrderFilterContent(
                initialFilters = listingData.filters,
                typeFilters = model.type,
                modifier
            ){ newFilters ->
                listingBaseViewModel.applyFilters(newFilters)
            }
        }

        ActiveWindowListingType.SORTING -> {
            SortingOrdersContent(
                listingData.sort,
                modifier
            ){ newSort ->
                listingBaseViewModel.applySorting(newSort)
            }
        }

        else -> {
            EdgeToEdgeScaffold(
                topBar = {
                    val filterBarUiState by listingBaseViewModel.filterBarUiState.collectAsState()
                    FiltersBar(
                        filterBarUiState,
                        isVisible = listingState.areBarsVisible.value,
                    )
                },
                onRefresh = {
                    viewModel.refresh()
                },
                error = error,
                noFound = noFound,
                isLoading = isLoading.value,
                toastItem = toastItem,
                modifier = modifier.fillMaxSize()
            ) { contentPadding ->
                PagingLayout(
                    data = data,
                    viewModel = listingBaseViewModel,
                    state = listingState.scrollState,
                    contentPadding = contentPadding,
                    content = { order ->
                        MyOrderItem(
                            order,
                            updateItem
                        )
                    }
                )
            }
        }
    }
}
