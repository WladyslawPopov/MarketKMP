package market.engine.presentation.profileMyOffers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = viewModel.listingData.value.data.subscribeAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val mainViewModel : MainViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        mainViewModel.sendEvent(
            UIMainEvent.UpdateFloatingActionButton {
                floatingCreateOfferButton {

                }
            }
        )
    }

    ListingBaseContent(
        columns = if (isBigScreen) 2 else 1,
        modifier = modifier,
        filtersData = viewModel.listingData,
        data = data,
        baseViewModel = viewModel,
        onRefresh = {
            data.refresh()
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
                onUpdateOfferItem = {
                    data.refresh()
                },
                onItemClick = {

                }
            )
        }
    )
}
