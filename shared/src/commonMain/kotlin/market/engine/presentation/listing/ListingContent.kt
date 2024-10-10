package market.engine.presentation.listing

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.ListingFiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.items.ColumnItemListing
import org.jetbrains.compose.resources.stringResource

const val SCROLL_DELTA_THRESHOLD = 60

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val searchData = component.globalData.listingData.searchData.subscribeAsState()
    val listingData = component.globalData.listingData.data.subscribeAsState()
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val result = model.listing
    val data = result.success?.collectAsLazyPagingItems()
    val offers by rememberUpdatedState(data)

    val isLoading : State<Boolean> = rememberUpdatedState(offers?.loadState?.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    val scrollState = rememberLazyGridState()

    data?.loadState?.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount < 1 -> {
                noItem = {
                    showNoItemLayout {
                        component.onRefresh()
                    }
                }
            }

            refresh is LoadStateError -> {
                error = {
                    onError(
                        ServerErrorException(
                            (data.loadState.refresh as LoadStateError).error.message ?: "", ""
                        )
                    ) { data.retry() }
                }
            }
        }
    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        topBar = {
            ListingAppBar(
                searchData.value.searchCategoryName ?: stringResource(strings.categoryMain),
                modifier,
                onSearchClick = {
                    component.goToSearch()
                },
                onBeakClick = {
                    component.onBackClicked()
                }
            )
        },
        onRefresh = { component.onRefresh() },
        error = error,
        noFound = noItem
    ){
        Column(modifier = Modifier.fillMaxSize()) {
            SwipeTabsBar(
                scrollState
            )
            ListingFiltersBar(
                listingData,
                searchData
            ){
                component.onRefresh()
            }
            Box(modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
            ) {
                if (offers != null) {
                    PagingGrid(
                        state = scrollState,
                        data = offers!!,
                        content = { offer ->
                           ColumnItemListing(
                               offer
                           )
                        }
                    )
                }
            }
        }
    }
}
