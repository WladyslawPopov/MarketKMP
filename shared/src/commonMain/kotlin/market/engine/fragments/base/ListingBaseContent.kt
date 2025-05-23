package market.engine.fragments.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.OfferItem
import market.engine.core.network.ServerErrorException
import market.engine.widgets.grids.PagingList

@Composable
fun <T : Any>ListingBaseContent(
    columns : MutableState<Int> = mutableStateOf(1),
    listingData : LD,
    searchData : SD,
    data : LazyPagingItems<T>,
    baseViewModel: BaseViewModel,
    onRefresh : () -> Unit,
    item : @Composable (T) -> Unit,
    noFound : (@Composable () -> Unit)? = null,
    filtersContent : (@Composable (MutableState<Boolean>, onClose : () ->Unit) -> Unit)? = null,
    additionalBar : @Composable (LazyListState) -> Unit = {},
    promoContent : (@Composable (OfferItem) -> Unit)? = null,
    promoList :  List<OfferItem>? = null,
    isReversingPaging : Boolean = false,
    scrollState:  LazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = baseViewModel.scrollItem.value,
        initialFirstVisibleItemScrollOffset = baseViewModel.offsetScrollItem.value
    ),
    modifier : Modifier = Modifier
){
    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(baseViewModel.bottomSheetState.value)
    )

    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    data.loadState.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount > 0 -> {
                error = null
                noItem = null
            }

            refresh is LoadStateNotLoading && data.itemCount < 1 -> {
                noItem = {
                    noFound?.invoke() ?: showNoItemLayout{onRefresh()}
                }
            }

            refresh is LoadStateError -> {
                baseViewModel.onError(
                    ServerErrorException(
                        errorCode = ((refresh as LoadStateError).error as? ServerErrorException)?.errorCode ?: "",
                        humanMessage = ((refresh as LoadStateError).error as? ServerErrorException)?.humanMessage ?: ""
                    )
                )
            }
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                baseViewModel.bottomSheetState.value = sheetValue
                if (sheetValue == BottomSheetValue.Collapsed) {
                    if (isRefreshingFromFilters.value) {
                        searchData.isRefreshing = true
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
            baseViewModel.scrollItem.value = index
            baseViewModel.offsetScrollItem.value = offset
        }
    }

    LaunchedEffect(baseViewModel.activeFiltersType.value){
        snapshotFlow {
            baseViewModel.activeFiltersType
        }.collect { type ->
            if (type.value.isNotEmpty()) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
                baseViewModel.activeFiltersType.value = ""
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if(baseViewModel.activeFiltersType.value == "" || baseViewModel.activeFiltersType.value == "categories"){
            additionalBar(scrollState)
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContentColor = colors.primaryColor,
            sheetBackgroundColor = colors.primaryColor,
            contentColor = colors.primaryColor,
            backgroundColor = colors.primaryColor,
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = false,
            sheetContent = {
                if (baseViewModel.activeFiltersType.value != "") {
                    filtersContent?.invoke(isRefreshingFromFilters) {
                        baseViewModel.activeFiltersType.value = ""
                    }
                }
            },
            modifier = Modifier.zIndex(1200f)
        ) {
            when {
                error != null -> {
                    LazyColumn {
                        item {
                            error.invoke()
                        }
                    }
                }
                noItem != null -> {
                    LazyColumn {
                        item {
                            noItem.invoke()
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = modifier
                    ) {
                        PagingList(
                            state = scrollState,
                            data = data,
                            listingData = listingData,
                            isReversingPaging = isReversingPaging,
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
