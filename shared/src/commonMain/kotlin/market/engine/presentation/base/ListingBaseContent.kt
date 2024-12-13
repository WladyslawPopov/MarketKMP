package market.engine.presentation.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.grids.PagingList

@Composable
fun <T : Any>ListingBaseContent(
    columns : MutableState<Int> = mutableStateOf(1),
    modifier: Modifier = Modifier,
    listingData : LD,
    searchData : SD,
    data : LazyPagingItems<T>,
    baseViewModel: BaseViewModel,
    onRefresh : () -> Unit,
    topBar : (@Composable () -> Unit)? = null,
    floatingActionButton : @Composable () -> Unit = {},
    item : @Composable (T) -> Unit,
    noFound : (@Composable () -> Unit)? = null,
    filtersContent : @Composable (MutableState<Boolean>, onClose : () ->Unit) -> Unit,
    sortingContent : @Composable (MutableState<Boolean>, onClose : () ->Unit) -> Unit,
    additionalBar : @Composable (LazyListState) -> Unit = {},
    promoContent : (@Composable (Offer) -> Unit)? = null,
    promoList :  ArrayList<Offer>? = null,
    isShowGrid : Boolean = false,
    onSearchClick : () -> Unit = {}
){
    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = listingData.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listingData.firstVisibleItemScrollOffset
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(listingData.bottomSheetState.value)
    )

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    data.loadState.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount > 0 && !isLoading.value -> {
                error = null
                noItem = null
            }

            refresh is LoadStateNotLoading && data.itemCount < 1 && !isLoading.value -> {
                noItem = {
                    noFound?.invoke() ?: showNoItemLayout{onRefresh()}
                }
            }
            refresh is LoadStateError && !isLoading.value -> {
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

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                listingData.bottomSheetState.value = sheetValue
                if (sheetValue == BottomSheetValue.Collapsed) {
                    if (isRefreshingFromFilters.value) {
                        scrollState.scrollToItem(0)
                        onRefresh()
                        isRefreshingFromFilters.value = false
                    }
                }
            }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            listingData.firstVisibleItemIndex = index
            listingData.firstVisibleItemScrollOffset = offset
        }
    }

    LaunchedEffect(listingData.activeFiltersType.value){
        snapshotFlow {
            listingData.activeFiltersType
        }.collect { type ->
            if (type.value.isNotEmpty()) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    LaunchedEffect(searchData.isRefreshing){
        if (searchData.isRefreshing){
            onRefresh()
            searchData.isRefreshing = false
        }
    }

    BaseContent(
        topBar = topBar,
        onRefresh = onRefresh,
        error = null,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = baseViewModel.toastItem,
        floatingActionButton = floatingActionButton,
        modifier = modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContentColor = colors.primaryColor,
            sheetBackgroundColor = colors.primaryColor,
            contentColor = colors.primaryColor,
            backgroundColor = colors.primaryColor,
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = false,
            sheetContent = {
                when (listingData.activeFiltersType.value) {
                    "filters" -> {
                        filtersContent(isRefreshingFromFilters) {
                            listingData.activeFiltersType.value = ""
                        }
                    }

                    "sorting" -> {
                        sortingContent(isRefreshingFromFilters) {
                            listingData.activeFiltersType.value = ""
                        }
                    }
                }
            },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                additionalBar(scrollState)

                FiltersBar(
                    listingData,
                    searchData,
                    isShowGrid = isShowGrid,
                    onChangeTypeList = {
                        baseViewModel.settings.setSettingValue("listingType", it)
                        listingData.listingType = it
                        onRefresh()
                    },
                    onFilterClick = {
                        listingData.activeFiltersType.value = "filters"
                    },
                    onSortClick = {
                        listingData.activeFiltersType.value = "sorting"
                    },
                    onSearchClick = {
                        onSearchClick()
                    },
                    onRefresh = { data.refresh() }
                )
                AnimatedVisibility(
                    listingData.updateItem.value == null, // update Paging Item(without refresh Paginator because it lost scroll position after refresh)
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .animateContentSize()
                    ) {
                        when{
                            error != null -> error?.invoke()
                            noItem != null -> noItem?.invoke()
                            else -> {
                                PagingList(
                                    state = scrollState,
                                    data = data,
                                    listingData = listingData,
                                    searchData = searchData,
                                    columns = columns.value,
                                    content = {
                                        item(it)
                                    },
                                    promoList = promoList,
                                    promoContent = promoContent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
