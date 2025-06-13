package market.engine.fragments.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.cash.paging.compose.LazyPagingItems
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.OfferItem
import market.engine.fragments.root.main.listing.ListingViewModel
import market.engine.widgets.grids.PagingList

data class ScrollDataState(
    val scrollItem : Int = 0,
    val offsetScrollItem : Int = 0,
)

data class ListingBaseData(
    val bottomSheetState : BottomSheetValue,
    val listingData : LD,
    val searchData : SD,
    val promoList: List<OfferItem>?,
    val isReversingPaging : Boolean = false,
    val activeWindowType: ListingViewModel.ActiveWindowType,
    val columns : Int = 1,
    val scrollDataState : ScrollDataState,
)

interface ListingBaseEvents{
   fun changeBottomSheetState(state : BottomSheetValue)
   fun saveScrollState(state : ScrollDataState)
}

@Composable
fun <T : Any>ListingBaseContent(
    uiState : ListingBaseData,
    events: ListingBaseEvents,
    modifier : Modifier = Modifier,
    data : LazyPagingItems<T>,
    item : @Composable (T) -> Unit,
    noFound : (@Composable () -> Unit)? = null,
    filtersContent : (@Composable () -> Unit)? = null,
    additionalBar : @Composable (LazyListState) -> Unit = {},
    promoContent : (@Composable (OfferItem) -> Unit)? = null,
    scrollState:  LazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.scrollDataState.scrollItem,
        initialFirstVisibleItemScrollOffset = uiState.scrollDataState.offsetScrollItem
    ),
){
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(uiState.bottomSheetState)
    )

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                events.changeBottomSheetState(sheetValue)
            }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            events.saveScrollState(ScrollDataState(index, offset))
        }
    }

    LaunchedEffect(uiState.activeWindowType){
        snapshotFlow {
            uiState.activeWindowType
        }.collect { type ->
            if (type != ListingViewModel.ActiveWindowType.LISTING) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            uiState.activeWindowType == ListingViewModel.ActiveWindowType.LISTING ||
                    uiState.activeWindowType == ListingViewModel.ActiveWindowType.CATEGORY,
            enter = fadeIn()
        ){
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
                if (uiState.activeWindowType != ListingViewModel.ActiveWindowType.LISTING) {
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
