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
import market.engine.fragments.base.listing.ListingBaseContent
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
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

    BaseContent(
        topBar = null,
        onRefresh = {
            component.onRefresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem.value,
        modifier = modifier.fillMaxSize()
    ) { contentPaddings ->
        ListingBaseContent(
            data = data,
            viewModel = listingBaseViewModel,
            noFound = noFound,
            contentPadding = contentPaddings,
            filtersContent = { activeWindowType ->
                when (activeWindowType) {
                    ActiveWindowListingType.FILTERS -> {
                        OfferFilterContent(
                            ld.filters,
                            categoryState,
                            LotsType.FAVORITES,
                        ){ newFilters ->
                            listingBaseViewModel.applyFilters(newFilters)
                        }
                    }

                    ActiveWindowListingType.SORTING -> {
                        SortingOffersContent(
                            ld.sort,
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
                    updateItem.value
                )
            },
        )
    }
}
