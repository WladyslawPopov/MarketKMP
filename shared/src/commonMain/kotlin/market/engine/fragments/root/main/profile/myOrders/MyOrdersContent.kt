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
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.filterContents.filters.OrderFilterContent
import market.engine.widgets.filterContents.sorts.SortingOrdersContent
import market.engine.widgets.items.MyOrderItem
import org.jetbrains.compose.resources.stringResource

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

    val dealType = model.type

    val typeGroup = remember {
        if (
            dealType in arrayOf(
                DealType.BUY_ARCHIVE,
                DealType.BUY_IN_WORK
            )
        ) DealTypeGroup.SELL else DealTypeGroup.BUY
    }

    val listingState = rememberLazyScrollState(viewModel)

    val noFound: @Composable (() -> Unit)? = remember(data.loadState.refresh, activeType) {
        when {
            activeType == ActiveWindowListingType.LISTING -> {
                if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
                    @Composable {
                        if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                            NoItemsFoundLayout(
                                textButton = stringResource(strings.resetLabel),
                                viewModel = viewModel,
                                goToOffer = { offer ->
                                    component.goToOffer(offer.id)
                                }
                            ) {
                                listingBaseViewModel.clearAllFilters()
                                viewModel.refresh()
                            }
                        } else {
                            NoItemsFoundLayout(
                                title = stringResource(strings.simpleNotFoundLabel),
                                icon = if (typeGroup == DealTypeGroup.SELL) drawables.purchasesIcon else drawables.salesIcon,
                                viewModel = viewModel,
                                goToOffer = { offer ->
                                    component.goToOffer(offer.id)
                                }
                            ) {
                                viewModel.refresh()
                            }
                        }
                    }
                } else {
                    null
                }
            }

            else -> {
                null
            }
        }
    }


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
