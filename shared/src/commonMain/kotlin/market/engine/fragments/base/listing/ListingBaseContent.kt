package market.engine.fragments.base.listing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.widgets.grids.PagingList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any>ListingBaseContent(
    modifier : Modifier = Modifier,
    viewModel: ListingBaseViewModel,
    scrollState : LazyListState = rememberLazyListState(),
    data : LazyPagingItems<T>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    item : @Composable (T) -> Unit,
    noFound : (@Composable () -> Unit)? = null,
    filtersContent : (@Composable (ActiveWindowListingType) -> Unit)? = null,
    promoContent : (@Composable (OfferItem) -> Unit)? = null,
){
    val activeType = viewModel.activeWindowType.collectAsState()
    val totalCount = viewModel.totalCount.collectAsState()
    val listingType = viewModel.listingType.collectAsState()
    val promoList = viewModel.promoList.collectAsState()
    val isReversingPaging = viewModel.isReversingPaging.collectAsState()

    val searchData = viewModel.listingData.value.searchData

    val sheetState = rememberStandardBottomSheetState(
        initialValue = if(activeType.value != ActiveWindowListingType.LISTING) SheetValue.Expanded else SheetValue.PartiallyExpanded
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    var bottomSheetContentType by remember { mutableStateOf(activeType.value) }

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                if(sheetValue == SheetValue.PartiallyExpanded && activeType.value != ActiveWindowListingType.LISTING){
                    viewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
                }
            }
    }

    LaunchedEffect(activeType.value) {
        if (activeType.value != ActiveWindowListingType.LISTING) {
            bottomSheetContentType = activeType.value
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContainerColor = colors.primaryColor,
        sheetContentColor = colors.primaryColor,
        contentColor = colors.primaryColor,
        containerColor = colors.primaryColor,
        sheetShape = MaterialTheme.shapes.small,
        sheetSwipeEnabled = false,
        sheetContent = {
            filtersContent?.invoke(bottomSheetContentType)
        },
    )
    {
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    PagingList(
                        state = scrollState,
                        data = data,
                        totalCount = totalCount.value,
                        isReversingPaging = isReversingPaging.value,
                        searchData = searchData,
                        columns = if (listingType.value == 0) 1 else 2,
                        contentPadding = contentPadding,
                        content = { data ->
                            item(data)
                        },
                        promoList = promoList.value,
                        promoContent = promoContent,
                    )
                }
            }
        }
    }
}
