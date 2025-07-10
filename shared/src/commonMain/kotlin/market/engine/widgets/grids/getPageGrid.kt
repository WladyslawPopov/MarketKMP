package market.engine.widgets.grids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.PAGE_SIZE
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DialogsData
import market.engine.core.data.items.OfferItem
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.network.networkObjects.Dialog
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.networkObjects.Subscription
import market.engine.widgets.bars.PagingCounterBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.rows.LazyRowWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T : Any> BoxScope.PagingList(
    data: LazyPagingItems<T>,
    state: LazyListState,
    columns : Int = 1,
    totalCount : Int = 0,
    searchData: SD? = null,
    promoList: List<OfferItem>? = null,
    isReversingPaging : Boolean = false,
    contentPadding: PaddingValues = PaddingValues(dimens.zero),
    filtersPadding: Dp = dimens.zero,
    promoContent: (@Composable (OfferItem) -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    var prevIndex by remember { mutableStateOf<Int?>(null) }
    var showUpButton by remember { mutableStateOf(false) }
    var showDownButton by remember { mutableStateOf(false) }

    val align = remember { if (isReversingPaging) Alignment.BottomStart else Alignment.TopStart }

    val currentIndex by remember {
        derivedStateOf {
            val allItems = data.itemSnapshotList.items
            val lastIndex = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            var count = 0
            for (i in 0..lastIndex.coerceAtMost(allItems.lastIndex)) {
                when {
                    allItems[i] !is DialogsData.SeparatorItem -> {
                        count++
                    }
                }
            }
            count
        }
    }

    LaunchedEffect(state.firstVisibleItemIndex){
        showUpButton = (state.firstVisibleItemIndex >= PAGE_SIZE)
        showDownButton = prevIndex != null &&
                state.firstVisibleItemIndex < (prevIndex ?: 0)
    }

    LazyColumnWithScrollBars(
        state = state,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        horizontalAlignment = Alignment.Start,
        contentPadding = dimens.smallPadding,
        reverseLayout = isReversingPaging,
        modifierList = Modifier
            .fillMaxSize()
            .align(align)
    ){
        item {
            Spacer(
                modifier = Modifier
                    .height(contentPadding.calculateTopPadding() + filtersPadding)
            )
        }

        if (!promoList.isNullOrEmpty() && promoContent != null) {
            item {
                LazyRowWithScrollBars(
                    heightMod = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
                ){
                    items(promoList) { offer ->
                        promoContent(offer)
                    }
                }
            }

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

        items(data.itemCount, key = { index ->
            when (val item = data.peek(index)) {
                is DialogsData.MessageItem -> "msg_${item.id}_$index"
                is DialogsData.SeparatorItem -> "separator_${item.dateTime}_$index"
                is Offer -> "offer_${item.id}_$index"
                is OfferItem -> "offerItem_${item.id}_$index"
                is Order -> "order_${item.id}_$index"
                is Conversations -> "conv_${item.id}_$index"
                is Dialog -> "dialog_${item.id}_$index"
                is Subscription -> "sub_${item.id}_$index"
                else -> "unknown_$index"
            }
        }
        ) { index ->
            if (promoContent != null && searchData?.userSearch == false && searchData.searchString.isEmpty()) {
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

        item {  }
    }

    PagingCounterBar(
        currentPage = currentIndex,
        totalPages = totalCount,
        modifier = Modifier.align(Alignment.BottomStart)
            .padding(bottom = contentPadding.calculateBottomPadding()),
        showUpButton = showUpButton,
        showDownButton = showDownButton,
        onClick = {
            when{
                showUpButton -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        prevIndex = currentIndex
                        state.scrollToItem(0)
                        showUpButton = false
                        showDownButton = true
                    }
                }
                showDownButton -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        state.scrollToItem(prevIndex ?: 1)
                        prevIndex = null
                        showDownButton = false
                        showUpButton = true
                    }
                }
            }
        }
    )
}
