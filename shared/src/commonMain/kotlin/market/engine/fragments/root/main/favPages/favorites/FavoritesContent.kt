package market.engine.fragments.root.main.favPages.favorites

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
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetOfferItem
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.plus

@Composable
fun FavoritesContent(
    component: FavoritesComponent,
    modifier: Modifier,
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val type = model.favType
    val viewModel = model.favViewModel
    val listingBaseViewModel = viewModel.listingBaseViewModel
    val categoryState = viewModel.filtersCategoryState

    val listingDataState by listingBaseViewModel.listingData.collectAsState()

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val err by viewModel.errorMessage.collectAsState()

    val updateItem by viewModel.updateItem.collectAsState()

    val ld = listingDataState.data

    val activeType by listingBaseViewModel.activeWindowType.collectAsState()
    val selectedItems by listingBaseViewModel.selectItems.collectAsState()

    val toastItem by viewModel.toastItem.collectAsState()

    val listingState = rememberLazyScrollState(viewModel)

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    BackHandler(model.backHandler){
        viewModel.onBackNavigation()
    }

    val noFound: @Composable (() -> Unit)? = remember(data.loadState.refresh, activeType) {

        when {
            activeType == ActiveWindowListingType.LISTING -> {
                if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
                    @Composable {
                        if (ld.filters.any { it.interpretation?.isNotBlank() == true }) {
                            NoItemsFoundLayout(
                                textButton = stringResource(strings.resetLabel)
                            ) {
                                listingBaseViewModel.clearListingData()
                                viewModel.refresh()
                            }
                        } else {
                            NoItemsFoundLayout(
                                title = stringResource(strings.emptyFavoritesLabel),
                                image = drawables.emptyFavoritesImage
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

    when (activeType) {
        ActiveWindowListingType.FILTERS -> {
            OfferFilterContent(
                ld.filters,
                categoryState,
                if(type == FavScreenType.FAVORITES) LotsType.FAVORITES else null,
                modifier
            ) { newFilters ->
                if(type == FavScreenType.FAV_LIST){
                    if (newFilters.contains(viewModel.offerListFilter)){
                        listingBaseViewModel.applyFilters(newFilters)
                    }else{
                        listingBaseViewModel.applyFilters(newFilters + viewModel.offerListFilter)

                    }
                }else{
                    listingBaseViewModel.applyFilters(newFilters)
                }
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
                        selectedItems.size,
                        onCancel = {
                            listingBaseViewModel.clearSelectedItems()
                        },
                        onDelete = {
                            listingBaseViewModel.deleteSelectedItems()
                        }
                    )

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
                    content = { offerRepos ->
                        CabinetOfferItem(
                            offerRepos,
                            updateItem,
                            selectedItems.contains(offerRepos.offerState.value.id),
                            onSelected = {
                                if (selectedItems.contains(offerRepos.offerState.value.id)) {
                                    listingBaseViewModel.removeSelectItem(offerRepos.offerState.value.id)
                                } else {
                                    listingBaseViewModel.addSelectItem(offerRepos.offerState.value.id)
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
