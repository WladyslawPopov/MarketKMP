package market.engine.presentation.subscriptions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.types.FavScreenType
import market.engine.presentation.base.BaseContent
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

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    BaseContent(
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
        onRefresh = {
            listingData.value.resetScroll()
            data.refresh()
        },
        error = null,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = subViewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            listingData = listingData.value,
            data = data,
            searchData = searchData.value,
            baseViewModel = subViewModel,
            onRefresh = {
                listingData.value.resetScroll()
                data.refresh()
            },
            filtersContent = { _, _ ->

            },
            sortingContent = { _, _ ->

            },
            item = {

            }
        )
    }
}
