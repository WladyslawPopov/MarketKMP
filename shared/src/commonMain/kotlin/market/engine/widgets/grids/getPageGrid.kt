package market.engine.widgets.grids

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.widgets.bars.PagingCounterBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T : Any> PagingList(
    data: LazyPagingItems<T>,
    state: LazyListState = rememberLazyListState(),
    columns : Int = 1,
    listingData: LD,
    searchData: SD? = null,
    promoList: ArrayList<Offer>? = null,
    promoContent: (@Composable (Offer) -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    var showUpButton by remember { mutableStateOf(false) }
    var showDownButton by remember { mutableStateOf(false) }

    val currentIndex by remember {
        derivedStateOf {
            state.firstVisibleItemIndex + if(listingData.totalCount > 1) 2 else 1
        }
    }

    LaunchedEffect(state.firstVisibleItemIndex){
        showUpButton = 2 < (state.firstVisibleItemIndex / listingData.pageCountItems)
        showDownButton = listingData.prevIndex != null &&
                state.firstVisibleItemIndex < (listingData.prevIndex ?: 0)
    }


    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = state,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
        ) {

            if (!promoList.isNullOrEmpty() && promoContent != null) {
                item {
                    LazyRow(
                        modifier = Modifier.height(300.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(promoList) { offer ->
                            promoContent(offer)
                        }
                    }
                }
            }

            if (promoContent != null) {
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

            items(data.itemCount) { index ->
                if (promoContent != null && searchData?.userSearch == false && searchData.searchString.isNullOrEmpty()) {
                    if (index > 0) {
                        val item = data[index] as? Offer
                        if (item?.promoOptions == null) {
                            val lastItem = data[index - 1] as? Offer
                            if (lastItem != null) {
                                val isLastItemPromo = lastItem.promoOptions?.any { it.id == "featured_in_listing" } == true
                                if (isLastItemPromo) {
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
                            val itemIndex = index + columnIndex
                            if (itemIndex < data.itemCount) {
                                val item = data[itemIndex]
                                Box(modifier = Modifier.weight(1f)) {
                                    item?.let { content(it) }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        PagingCounterBar(
            currentPage = currentIndex,
            totalPages = listingData.totalCount,
            modifier = Modifier.align(Alignment.BottomStart),
            showUpButton = showUpButton,
            showDownButton = showDownButton,
            onClick = {
                when{
                    showUpButton -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            listingData.prevIndex = currentIndex
                            state.scrollToItem(0)
                            showUpButton = false
                            showDownButton = true
                        }
                    }
                    showDownButton -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            state.scrollToItem(listingData.prevIndex ?: 1)
                            listingData.prevIndex = null
                            showDownButton = false
                            showUpButton = true
                        }
                    }
                }
            }
        )
    }
}
