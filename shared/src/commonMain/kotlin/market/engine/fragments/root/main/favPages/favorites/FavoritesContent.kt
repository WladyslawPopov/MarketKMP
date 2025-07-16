package market.engine.fragments.root.main.favPages.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetOfferItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavoritesContent(
    component: FavoritesComponent,
    modifier: Modifier,
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val viewModel = model.favViewModel
    val listingBaseViewModel = viewModel.listingBaseViewModel
    val categoryState = viewModel.filtersCategoryState

    val listingDataState = listingBaseViewModel.listingData.collectAsState()

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val err = viewModel.errorMessage.collectAsState()

    val updateItem = viewModel.updateItem.collectAsState()

    val ld = listingDataState.value.data

    val activeType = listingBaseViewModel.activeWindowType.collectAsState()
    val selectedItems = listingBaseViewModel.selectItems.collectAsState()
    val filterBarUiState = listingBaseViewModel.filterBarUiState.collectAsState()
    val listingState = rememberLazyScrollState(viewModel)

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    BackHandler(model.backHandler){
        viewModel.onBackNavigation()
    }

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (ld.filters.any { it.interpretation != null && it.interpretation != "" }) {
                    NoItemsFoundLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        listingBaseViewModel.clearAllFilters()
                    }
                } else {
                    NoItemsFoundLayout(
                        title = stringResource(strings.emptyFavoritesLabel),
                        image = drawables.emptyFavoritesImage
                    ) {
                        component.onRefresh()
                    }
                }
            }
        } else {
            null
        }
    }

    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            { OnError(err.value) { component.onRefresh() } }
        } else {
            null
        }
    }

    when (activeType.value) {
        ActiveWindowListingType.FILTERS -> {
            OfferFilterContent(
                ld.filters,
                categoryState,
                LotsType.FAVORITES,
                modifier
            ) { newFilters ->
                listingBaseViewModel.applyFilters(newFilters)
            }
        }

        ActiveWindowListingType.SORTING -> {
            SortingOffersContent(
                ld.sort,
                isCabinet = true,
                modifier,
                onClose = { newSort ->
                    listingBaseViewModel.applySorting(newSort)
                }
            )
        }

        else -> {
            EdgeToEdgeScaffold(
                topBar = {
                    DeletePanel(
                        selectedItems.value.size,
                        onCancel = {
                            listingBaseViewModel.clearSelectedItems()
                        },
                        onDelete = {
                            listingBaseViewModel.deleteSelectedItems()
                        }
                    )

                    FiltersBar(
                        filterBarUiState.value,
                        isVisible = listingState.areBarsVisible.value,
                    )
                },
                onRefresh = {
                    component.onRefresh()
                },
                error = error,
                noFound = noFound,
                isLoading = isLoading.value,
                toastItem = viewModel.toastItem.value,
                modifier = modifier.fillMaxSize()
            ) { contentPadding ->
                PagingLayout(
                    data = data,
                    viewModel = listingBaseViewModel,
                    state = listingState.scrollState,
                    contentPadding = contentPadding,
                    content = { offer ->
                        CabinetOfferItem(
                            offer,
                            updateItem.value,
                            selectedItems.value.contains(offer.item.id),
                            onSelected = {
                                if (selectedItems.value.contains(offer.item.id)) {
                                    listingBaseViewModel.removeSelectItem(offer.item.id)
                                } else {
                                    listingBaseViewModel.addSelectItem(offer.item.id)
                                }

                            }
                        )
                    },
                )
            }
        }
    }
}
