package market.engine.presentation.listing

import market.engine.widgets.items.ColumnItemListing
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.russhwolf.settings.set
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.network.ServerErrorException
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.ListingFiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.FilterContent
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.items.GridItemListing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val listingViewModel = modelState.value.listingViewModel
    val searchData = listingViewModel.listingData.searchData.subscribeAsState()
    val listingData = listingViewModel.listingData.data.subscribeAsState()
    val data = listingViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val regions = listingViewModel.regionOptions.value

    val scrollState = rememberLazyGridState(
        initialFirstVisibleItemIndex = listingViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listingViewModel.firstVisibleItemScrollOffset
    )
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val activeFiltersType = remember { mutableStateOf("") }
    val isHideContent = remember { mutableStateOf(false) }
    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            listingViewModel.firstVisibleItemIndex = index
            listingViewModel.firstVisibleItemScrollOffset = offset
        }
    }

    LaunchedEffect(searchData){
        if (searchData.value.isRefreshing) {
            searchData.value.isRefreshing = false
            if (listingData.value.filters == null)
                listingData.value.filters = EmptyFilters.getEmpty()
            component.onRefresh()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                if (sheetValue == BottomSheetValue.Collapsed) {
                    if (isRefreshingFromFilters.value) {
                        component.onRefresh()
                        isRefreshingFromFilters.value = false
                    }
                }
            }
    }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    data.loadState.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount < 1 -> {
                isHideContent.value = true
                noItem = {
                    showNoItemLayout {
                        searchData.value.clear()
                        listingData.value.filters = EmptyFilters.getEmpty()
                        component.onRefresh()
                    }
                }
            }

            refresh is LoadStateError -> {
                isHideContent.value = true
                error = {
                    onError(
                        ServerErrorException(
                            (data.loadState.refresh as LoadStateError).error.message ?: "", ""
                        )
                    ) {
                        data.retry()
                    }
                }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        sheetBackgroundColor = colors.primaryColor,
        sheetPeekHeight = 0.dp,
        sheetGesturesEnabled = false,
        sheetContent = {
            when (activeFiltersType.value) {
                "filters" -> {
                    FilterContent(
                        isRefreshingFromFilters,
                        listingData,
                        scaffoldState,
                        scope,
                        regions
                    )
                }

                "sorting" -> {
                    SortingListingContent(
                        isRefreshingFromFilters,
                        listingData,
                        scaffoldState,
                        scope,
                    )
                }
            }
        },
    ) {
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
            onRefresh = {
                component.onRefresh()
            },
        ) {
            Column(modifier = Modifier.background(colors.primaryColor).fillMaxSize()) {

                SwipeTabsBar(
                    listingData,
                    scrollState,
                    onRefresh = {
                        component.onRefresh()
                    }
                )

                ListingFiltersBar(
                    listingData,
                    searchData,
                    onChangeTypeList = {
                        component.model.value.listingViewModel.settings["listingType"] =
                            it
                        component.onRefresh()
                    },
                    onFilterClick = {
                        activeFiltersType.value = "filters"
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                            scaffoldState.bottomSheetState.expand()
                        }

                    },
                    onSortClick = {
                        activeFiltersType.value = "sorting"
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    onRefresh = { component.onRefresh() }
                )

                if (error != null) {
                    error!!()
                } else {
                    if (noItem != null) {
                        noItem!!()
                    } else {
                        Box(
                            modifier = Modifier
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
                                                val currentOffer =
                                                    data[data.itemSnapshotList.items.indexOf(
                                                        it
                                                    )]
                                                if (currentOffer != null) {
                                                    val result = scope.async {
                                                        val item =
                                                            data[data.itemSnapshotList.items.indexOf(
                                                                currentOffer
                                                            )]
                                                        if (item != null) {
                                                            component.addToFavorites(item)
                                                        } else {
                                                            return@async currentOffer.isWatchedByMe
                                                        }
                                                    }
                                                    result.await()
                                                } else {
                                                    return@ColumnItemListing it.isWatchedByMe
                                                }
                                            }
                                        )
                                    } else {
                                        GridItemListing(
                                            offer,
                                            onFavouriteClick = {
                                                val currentOffer =
                                                    data[data.itemSnapshotList.items.indexOf(
                                                        it
                                                    )]
                                                if (currentOffer != null) {
                                                    val result = scope.async {
                                                        val item =
                                                            data[data.itemSnapshotList.items.indexOf(
                                                                currentOffer
                                                            )]
                                                        if (item != null) {
                                                            component.addToFavorites(item)
                                                        } else {
                                                            return@async currentOffer.isWatchedByMe
                                                        }
                                                    }
                                                    result.await()
                                                } else {
                                                    return@GridItemListing it.isWatchedByMe
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}




