package market.engine.presentation.profileMyOffers

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    drawerState: DrawerState,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = viewModel.listingData.value.data.subscribeAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    ListingBaseContent(
        columns = columns,
        modifier = modifier,
        listingData,
        searchData = viewModel.listingData.value.searchData.subscribeAsState(),
        data = data,
        baseViewModel = viewModel,
        onRefresh = {
            data.refresh()
        },
        topBar = {
            ProfileMyOffersAppBar(
                model.type,
                drawerState = drawerState,
                navigationClick = { newType->
                    component.selectMyOfferPage(newType)
                }
            )
        },
        floatingActionButton = {
            floatingCreateOfferButton {

            }
        },
        filtersContent = { isRefreshingFromFilters, onClose ->
            OfferFilterContent(
                isRefreshingFromFilters,
                listingData,
                model.type,
                onClose
            )
        },
        sortingContent = { isRefreshingFromFilters, onClose ->
            SortingListingContent(
                isRefreshingFromFilters,
                listingData,
                onClose
            )
        },
        item = { offer->
            MyOffersItem(
                offer = offer,
                viewModel,
                onUpdateOfferItem = {
                    data.refresh()
                },
                onItemClick = {
                    component.goToOffer(offer)
                }
            )
        }
    )
}
