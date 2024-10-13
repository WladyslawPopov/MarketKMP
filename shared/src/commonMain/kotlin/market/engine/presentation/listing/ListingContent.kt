package market.engine.presentation.listing

import market.engine.widgets.items.ColumnItemListing
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.snapshotFlow
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.russhwolf.settings.set
import kotlinx.coroutines.async
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.items.GridItemListing
import market.engine.widgets.items.PromoLotItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val searchData = component.globalData.listingData.searchData.subscribeAsState()
    val listingData = component.globalData.listingData.data.subscribeAsState()

    val modelState = component.model.subscribeAsState()
    val listingViewModel = modelState.value.listingViewModel

    val data = listingViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val scrollState = rememberLazyGridState(
        initialFirstVisibleItemIndex = listingViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listingViewModel.firstVisibleItemScrollOffset
    )

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                listingViewModel.firstVisibleItemIndex = index
                listingViewModel.firstVisibleItemScrollOffset = offset
            }
    }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    data.loadState.apply {
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
                listingData,
                scrollState
            )
            ListingFiltersBar(
                listingData,
                searchData,
                onChangeTypeList = {
                    component.model.value.listingViewModel.settings["listingType"] = it
                    component.onRefresh()
                },
                onRefresh = { component.onRefresh() }
            )

            Box(modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
            ) {
                PagingGrid(
                    state = scrollState,
                    data = data,
                    listingData = listingData,
                    content = { offer ->
                        if (listingData.value.listingType == 0) {
                            ColumnItemListing(
                                offer,
                                onFavouriteClick = {
                                    val scope = component.model.value.listingViewModel.viewModelScope
                                    val currentOffer = data[data.itemSnapshotList.items.indexOf(it)]
                                    if (currentOffer != null) {
                                        val result = scope.async {
                                            val item = data[data.itemSnapshotList.items.indexOf(
                                                currentOffer
                                            )]
                                            if (item != null) {
                                                component.addToFavorites(item)
                                            } else {
                                                return@async currentOffer.isWatchedByMe
                                            }
                                        }
                                        result.await()
                                    }else{
                                        return@ColumnItemListing it.isWatchedByMe
                                    }
                                }
                            )
                        }else{
                            GridItemListing(
                                offer,
                                onFavouriteClick = {
                                    val scope = component.model.value.listingViewModel.viewModelScope
                                    val currentOffer = data[data.itemSnapshotList.items.indexOf(it)]
                                    if (currentOffer != null) {
                                        val result = scope.async {
                                            val item = data[data.itemSnapshotList.items.indexOf(
                                                currentOffer
                                            )]
                                            if (item != null) {
                                                component.addToFavorites(item)
                                            } else {
                                                return@async currentOffer.isWatchedByMe
                                            }
                                        }
                                        result.await()
                                    }else{
                                        return@GridItemListing it.isWatchedByMe
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }

        Box(
            modifier.fillMaxWidth().fillMaxHeight()
        ){
            Card(
                modifier.align(Alignment.BottomStart)
            ){

            }
        }
    }
}
