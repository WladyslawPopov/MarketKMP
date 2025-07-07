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
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.OnError
import market.engine.fragments.base.NoItemsFoundLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
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

    val activeWindowType = listingBaseViewModel.activeWindowType.collectAsState()

    val listingData = listingDataState.value.data

    val categoriesData = viewModel.categoryState

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
                        icon = drawables.bidsIcon
                    ) {
                        viewModel.updatePage()
                    }
                }
            }
        } else {
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
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            data = data,
            viewModel = listingBaseViewModel,
            noFound = noFound,
            filtersContent = {
                when (activeWindowType.value) {
                    ActiveWindowListingType.FILTERS -> {
                        OfferFilterContent(
                            listingData.filters,
                            categoriesData,
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
                CabinetBidsItem(
                    offer,
                    updateItem.value,
                )
            }
        )
    }
}
