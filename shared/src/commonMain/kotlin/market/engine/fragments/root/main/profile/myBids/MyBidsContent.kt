package market.engine.fragments.root.main.profile.myBids


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
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.filterContents.filters.OfferFilterContent
import market.engine.widgets.filterContents.sorts.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetBidsItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyBidsContent(
    component: MyBidsComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingBaseViewModel = viewModel.listingBaseViewModel

    val listingDataState = listingBaseViewModel.listingData.collectAsState()

    val activeType by listingBaseViewModel.activeWindowType.collectAsState()

    val listingData = listingDataState.value.data

    val categoriesData = viewModel.categoryState

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem by viewModel.updateItem.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val err by viewModel.errorMessage.collectAsState()

    val toastItem by viewModel.toastItem.collectAsState()

    val noFound: @Composable (() -> Unit)? = remember(data.loadState.refresh, activeType) {
        when {
            activeType == ActiveWindowListingType.LISTING -> {
                if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
                    @Composable {
                        if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                            NoItemsFoundLayout(
                                textButton = stringResource(strings.resetLabel)
                            ) {
                                listingBaseViewModel.clearAllFilters()
                                viewModel.refresh()
                            }
                        } else {
                            NoItemsFoundLayout(
                                title = stringResource(strings.simpleNotFoundLabel),
                                icon = drawables.bidsIcon
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

    val error : (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage != "") {
            { OnError(err) { viewModel.refresh() } }
        } else {
            null
        }
    }

    val listingState = rememberLazyScrollState(viewModel)

    when(activeType){
        ActiveWindowListingType.FILTERS -> {
            OfferFilterContent(
                listingData.filters,
                categoriesData,
                viewModel.type,
                modifier
            ){ newFilters ->
                listingBaseViewModel.applyFilters(newFilters)
            }
        }

        ActiveWindowListingType.SORTING -> {
            SortingOffersContent(
                listingData.sort,
                isCabinet = true,
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
                        isVisible = listingState.areBarsVisible.value
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
            ) { padding ->
                PagingLayout(
                    data = data,
                    viewModel = listingBaseViewModel,
                    state = listingState.scrollState,
                    contentPadding = padding,
                    content = { offer ->
                        CabinetBidsItem(
                            offer,
                            updateItem,
                        )
                    }
                )
            }
        }
    }
}
