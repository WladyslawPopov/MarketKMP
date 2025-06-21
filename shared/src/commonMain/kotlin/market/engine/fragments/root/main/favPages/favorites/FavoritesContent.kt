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
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.dialogs.OfferOperationsDialogs
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
    val uiState = viewModel.favDataState.collectAsState()
    val listingData = uiState.value.listingData
    val categoriesData = uiState.value.filtersCategoryState

    val activeWindowType = uiState.value.listingBaseState.activeWindowType

    val listingBaseData = uiState.value.listingBaseState
    val filterBarState = uiState.value.filterBarData

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val err = viewModel.errorMessage.collectAsState()

    val updateItem = viewModel.updateItem.collectAsState()
    val dialogFields = viewModel.fieldsDialog.collectAsState()
    val dialogTitle = viewModel.titleDialog.collectAsState()
    val openDialog = viewModel.showOperationsDialog.collectAsState()
    val itemIdDialog = viewModel.dialogItemId.collectAsState()

    val ld = listingData.data

    val selectedItems = remember { viewModel.selectItems }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    BackHandler(model.backHandler){
        viewModel.onBackNavigation(activeWindowType)
    }

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (ld.filters.any { it.interpretation != null && it.interpretation != "" }) {
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        viewModel.clearAllFilters()
                    }
                } else {
                    showNoItemLayout(
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
            { onError(err.value) { component.onRefresh() } }
        } else {
            null
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            component.onRefresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            uiState= listingBaseData,
            data = data,
            baseViewModel = viewModel,
            noFound = noFound,
            filtersContent = {
                when (activeWindowType) {
                    ActiveWindowListingType.FILTERS -> {
                        OfferFilterContent(
                            ld.filters,
                            categoriesData,
                            LotsType.FAVORITES,
                        ){ newFilters ->
                            viewModel.applyFilters(newFilters)
                        }
                    }

                    ActiveWindowListingType.SORTING -> {
                        SortingOffersContent(
                            ld.sort,
                            isCabinet = true,
                        ){ newSort ->
                            viewModel.applySorting(newSort)
                        }
                    }
                    else -> {

                    }
                }
            },
            additionalBar = {
                DeletePanel(
                    selectedItems.size,
                    onCancel = {
                        viewModel.selectItems.clear()
                    },
                    onDelete = {
                        viewModel.deleteSelectsItems(selectedItems)
                    }
                )

                FiltersBar(
                    filterBarState
                )
            },
            item = { offer ->
                CabinetOfferItem(
                    offer,
                    updateItem.value
                )
            },
            modifier = modifier
        )

        OfferOperationsDialogs(
            offerId = itemIdDialog.value,
            showDialog = openDialog.value,
            viewModel = viewModel,
            title = dialogTitle.value,
            fields = dialogFields.value,
            updateItem = {
                viewModel.updateItem.value = itemIdDialog.value
            },
            close = { fullRefresh ->
                viewModel.clearDialogFields()
                if (fullRefresh) {
                    component.onRefresh()
                    component.refreshTabs()
                }
            }
        )
    }
}
