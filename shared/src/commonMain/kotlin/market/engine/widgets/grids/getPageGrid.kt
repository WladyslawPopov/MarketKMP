package market.engine.widgets.grids

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.widgets.bars.PagingCounterBar
import market.engine.widgets.buttons.getFloatAnyButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T : Any> PagingList(
    data: LazyPagingItems<T>,
    promoList: ArrayList<Offer>? = null,
    state: LazyListState = rememberLazyListState(),
    columns : Int = 1,
    listingData: State<LD>,
    searchData: State<SD>? = null,
    fromListing: Boolean = false,
    promoContent: (@Composable (Offer) -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    val itemsPerPage = listingData.value.pageCountItems
    val totalPages = listingData.value.totalPages

    var previousIndex by remember { mutableStateOf(0) }
    var showUpButton by remember { mutableStateOf(false) }
    var showDownButton by remember { mutableStateOf(true) }

    val currentPage by remember {
        derivedStateOf {
            (state.firstVisibleItemIndex / itemsPerPage) + 1
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex < previousIndex) {
                    showUpButton = true
                    showDownButton = false
                } else if (currentIndex > previousIndex) {
                    showUpButton = false
                    showDownButton = true
                }

                if (currentIndex == 0) {
                    showUpButton = false
                }

                if (currentPage == totalPages) {
                    showDownButton = false
                    showUpButton = false
                }

                previousIndex = currentIndex
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
        ) {

            if (!promoList.isNullOrEmpty() && fromListing) {
                item {
                    LazyRow(
                        modifier = Modifier.height(400.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(promoList) { offer ->
                            if (promoContent != null) {
                                promoContent(offer)
                            }
                        }
                    }
                }
            }
            if (fromListing) {
                if ((data.itemSnapshotList.items.firstOrNull() as? Offer)?.promoOptions != null) {
                    item {
                        Text(
                            text = stringResource(strings.topOffersTitle),
                            color = colors.black,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(dimens.smallPadding)
                        )
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(strings.offersLabel),
                            color = colors.black,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(dimens.smallPadding)
                        )
                    }
                }
            }

            var isShowEndPromo = false
            items(data.itemCount) { index ->
                if (fromListing && searchData?.value?.userSearch == false && searchData.value.searchString.isNullOrEmpty()) {
                    if (index > 0 && !isShowEndPromo) {
                        val item = data[index] as? Offer
                        if (item?.promoOptions == null) {
                            val lastItem = data[index - 1] as? Offer
                            if (lastItem != null) {
                                val isLastItemPromo =
                                    lastItem.promoOptions?.any { it.id == "featured_in_listing" } == true

                                if (isLastItemPromo) {
                                    isShowEndPromo = true
                                    Text(
                                        text = stringResource(strings.promoEndLabel),
                                        color = colors.titleTextColor,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .padding(dimens.smallPadding)
                                    )
                                }
                            }
                        }
                    }
                }

                if (index % columns == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        for (columnIndex in 0 until columns) {
                            val item = data[index + columnIndex]
                            Box(modifier = Modifier.weight(1f)) {
                                item?.let { content(it) }
                            }
                        }
                    }
                }
            }
        }

        PagingCounterBar(
            currentPage = currentPage,
            totalPages = listingData.value.totalPages,
            modifier = Modifier.align(Alignment.BottomStart)
        )

//        if (showUpButton) {
//            getFloatAnyButton(
//                drawable = drawables.iconArrowUp,
//                modifier = Modifier.align(Alignment.BottomEnd)
//            ) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    scrollToPreviousPage(state, itemsPerPage)
//                }
//            }
//        }
//
//        if (showDownButton) {
//            getFloatAnyButton(
//                drawable = drawables.iconArrowDown,
//                modifier = Modifier.align(Alignment.BottomEnd)
//            ) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    scrollToNextPage(state, itemsPerPage)
//                }
//            }
//        }
    }
}

private suspend fun scrollToPreviousPage(state: LazyListState, itemsPerPage: Int) {
    val previousPageFirstItemIndex = maxOf(state.firstVisibleItemIndex - itemsPerPage, 0)
    state.scrollToItem(previousPageFirstItemIndex)
}

private suspend fun scrollToNextPage(state: LazyListState, itemsPerPage: Int) {
    val nextPageFirstItemIndex = minOf(
        state.firstVisibleItemIndex + itemsPerPage,
        state.layoutInfo.totalItemsCount - 1
    )
    state.scrollToItem(nextPageFirstItemIndex)
}
