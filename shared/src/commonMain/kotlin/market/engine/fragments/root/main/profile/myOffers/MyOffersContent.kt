package market.engine.fragments.root.main.profile.myOffers

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
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.listing.listingNotFoundView
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetOfferItem

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingBaseViewModel = viewModel.listingBaseViewModel
    val categoryState = viewModel.categoryState

    val listingDataState by listingBaseViewModel.listingData.collectAsState()
    val activeType by listingBaseViewModel.activeWindowType.collectAsState()

    val listingData = listingDataState.data
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem by viewModel.updateItem.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val err by viewModel.errorMessage.collectAsState()
    val toastItem by viewModel.toastItem.collectAsState()

    BackHandler(model.backHandler){
        viewModel.onBackNavigation()
    }

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

    val error : (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage != "") {
            { OnError(err) { viewModel.refresh() } }
        } else {
            null
        }
    }

    val listingState = rememberLazyScrollState(viewModel)

    when(activeType) {
        ActiveWindowListingType.FILTERS -> {
            OfferFilterContent(
                listingData.filters,
                categoryState,
                LotsType.FAVORITES,
                modifier
            ) { newFilters ->
                listingBaseViewModel.applyFilters(newFilters)
            }
        }

        ActiveWindowListingType.SORTING -> {
            SortingOffersContent(
                listingData.sort,
                isCabinet = true,
                modifier
            ) { newSort ->
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
                floatingActionButton = {
                    floatingCreateOfferButton {
                        component.goToCreateOffer(CreateOfferType.CREATE, null, null)
                    }
                },
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
                            updateItem,
                        )
                    }
                )
            }
        }
    }
}
