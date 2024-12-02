package market.engine.presentation.subscriptions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.types.FavScreenType
import market.engine.presentation.base.ListingBaseContent
import market.engine.presentation.favorites.FavoritesAppBar

@Composable
fun SubscribesContent(
    modifier: Modifier,
    component: SubscribesComponent,
) {
    val modelState = component.model.subscribeAsState()
    val subViewModel = modelState.value.subViewModel
    val searchData = subViewModel.listingData.value.searchData.subscribeAsState()
    val listingData = subViewModel.listingData.value.data.subscribeAsState()
    val data = subViewModel.pagingDataFlow.collectAsLazyPagingItems()

    ListingBaseContent(
        topBar = {
            FavoritesAppBar(
                FavScreenType.SUBSCRIBED,
                modifier
            ) { type ->
                if (type == FavScreenType.FAVORITES) {
                    component.goToFavorites()
                }
            }
        },
        modifier = modifier,
        listingData = listingData.value,
        data = data,
        searchData = searchData.value,
        baseViewModel = subViewModel,
        onRefresh = {
            subViewModel.refresh()
        },
        filtersContent = { _, _ ->

        },
        sortingContent = { _, _ ->

        },
        item = {

        }
    )
}
