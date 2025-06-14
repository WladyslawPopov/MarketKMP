package market.engine.fragments.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.cash.paging.compose.LazyPagingItems
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.OfferItem
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.ScrollDataState
import market.engine.fragments.root.main.listing.ActiveWindowType
import market.engine.widgets.grids.PagingList


@Composable
fun <T : Any>ListingBaseContent(
    modifier : Modifier = Modifier,
    uiState : ListingBaseState,
    baseViewModel: BaseViewModel,
    data : LazyPagingItems<T>,
    item : @Composable (T) -> Unit,
    noFound : (@Composable () -> Unit)? = null,
    filtersContent : (@Composable () -> Unit)? = null,
    additionalBar : @Composable (LazyListState) -> Unit = {},
    promoContent : (@Composable (OfferItem) -> Unit)? = null,
){
    val scrollStateData = baseViewModel.scrollState.collectAsState()
    val bottomSheetState = baseViewModel.bottomSheetState.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(bottomSheetState.value)
    )

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollStateData.value.scrollItem,
        initialFirstVisibleItemScrollOffset = scrollStateData.value.offsetScrollItem
    )

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                baseViewModel.bottomSheetState.value = sheetValue
            }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            baseViewModel.scrollState.value = ScrollDataState(index, offset)
        }
    }

    LaunchedEffect(uiState.activeWindowType){
        snapshotFlow {
            uiState.activeWindowType
        }.collect { type ->
            if (type != ActiveWindowType.LISTING) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.activeWindowType == ActiveWindowType.LISTING ||
                    uiState.activeWindowType == ActiveWindowType.CATEGORY){
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
                if (uiState.activeWindowType != ActiveWindowType.LISTING) {
                    filtersContent?.invoke()
                }
            },
            modifier = Modifier.zIndex(120f).weight(1f)
        ) {
            when {
                noFound != null -> {
                    LazyColumn {
                        item {
                            noFound.invoke()
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
                            listingData = uiState.listingData,
                            isReversingPaging = uiState.isReversingPaging,
                            searchData = uiState.searchData,
                            columns = uiState.columns,
                            content = { data ->
                                item(data)
                            },
                            promoList = uiState.promoList,
                            promoContent = promoContent,
                        )
                    }
                }
            }
        }
    }
}
