package market.engine.fragments.root.main.profile.myOffers

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
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetOfferItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val uiState = viewModel.uiDataState.collectAsState()
    val listingData = uiState.value.listingData.data
    val activeWindowType = uiState.value.listingBaseState.activeWindowType
    val listingBaseState = uiState.value.listingBaseState
    val filterBarUiState = uiState.value.filterBarData
    val categoriesData = uiState.value.filtersCategoryState

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem = viewModel.updateItem.collectAsState()

    val dialogFields = viewModel.fieldsDialog.collectAsState()
    val dialogTitle = viewModel.titleDialog.collectAsState()
    val openDialog = viewModel.showOperationsDialog.collectAsState()
    val itemIdDialog = viewModel.dialogItemId.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val err = viewModel.errorMessage.collectAsState()

    BackHandler(model.backHandler){
        viewModel.onBackNavigation(activeWindowType)
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
                        icon = drawables.emptyOffersIcon
                    ) {
                        viewModel.updatePage()
                    }
                }
            }
        }else{
            null
        }
    }

    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            { onError(err.value) { viewModel.updatePage() } }
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
        floatingActionButton = {
            floatingCreateOfferButton {
                component.goToCreateOffer(CreateOfferType.CREATE, null, null)
            }
        },
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
                        OfferFilterContent(
                            listingData.filters,
                            categoriesData,
                            LotsType.FAVORITES,
                        ){ newFilters ->
                            viewModel.applyFilters(newFilters)
                        }
                    }

                    ActiveWindowListingType.SORTING -> {
                        SortingOffersContent(
                            listingData.sort,
                            isCabinet = true,
                        ){ newSort ->
                            viewModel.applySorting(newSort)
                        }
                    }
                    else -> {

                    }
                }
            },
            item = { offer ->
                CabinetOfferItem(
                    offer,
                    updateItem.value,
                )
            }
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
                }
            }
        )
    }
}
