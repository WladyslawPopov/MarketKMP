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
import market.engine.fragments.base.listing.ListingBaseContent
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
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
    val listingBaseViewModel = viewModel.listingBaseViewModel
    val categoryState = viewModel.categoryState

    val listingDataState by listingBaseViewModel.listingData.collectAsState()
    val activeWindowType by listingBaseViewModel.activeWindowType.collectAsState()

    val listingData = listingDataState.data
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem = viewModel.updateItem.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val err = viewModel.errorMessage.collectAsState()
    val toastItem = viewModel.toastItem.collectAsState()

    BackHandler(model.backHandler){
        viewModel.onBackNavigation()
    }

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                    NoItemsFoundLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        listingBaseViewModel.clearAllFilters()
                        viewModel.updatePage()
                    }
                } else {
                    NoItemsFoundLayout(
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
            { OnError(err.value) { viewModel.updatePage() } }
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
        toastItem = toastItem.value,
        floatingActionButton = {
            floatingCreateOfferButton {
                component.goToCreateOffer(CreateOfferType.CREATE, null, null)
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            data = data,
            viewModel = listingBaseViewModel,
            noFound = noFound,
            filtersContent = {
                when (activeWindowType) {
                    ActiveWindowListingType.FILTERS -> {
                        OfferFilterContent(
                            listingData.filters,
                            categoryState,
                            LotsType.FAVORITES,
                        ){ newFilters ->
                            listingBaseViewModel.applyFilters(newFilters)
                        }
                    }

                    ActiveWindowListingType.SORTING -> {
                        SortingOffersContent(
                            listingData.sort,
                            isCabinet = true,
                        ){ newSort ->
                            listingBaseViewModel.applySorting(newSort)
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
    }
}
