package market.engine.presentation.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.NavigationItem
import market.engine.core.network.ServerErrorException
import market.engine.core.types.TabTypeListing
import market.engine.presentation.base.BaseContent
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.items.PromoLotItem
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

const val SCROLL_DELTA_THRESHOLD = 60

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val searchData = component.globalData.listingData.searchData.subscribeAsState()
    val listingData = component.globalData.listingData.data.subscribeAsState()

    var selectedTab by remember { mutableStateOf(TabTypeListing.ALL) }
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val result = model.listing
    val offers = result.success?.collectAsLazyPagingItems()

    val isLoading : State<Boolean> = rememberUpdatedState(offers?.loadState?.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    val scrollState = rememberLazyGridState()
    var previousScrollIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }

    var isTabsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState.firstVisibleItemScrollOffset, scrollState.firstVisibleItemIndex) {
        val currentOffset = scrollState.firstVisibleItemScrollOffset
        val currentIndex = scrollState.firstVisibleItemIndex

        // Рассчитываем общее смещение
        val totalOffsetChange = (currentIndex - previousScrollIndex) * 1000 + (currentOffset - previousScrollOffset)

        if (totalOffsetChange > SCROLL_DELTA_THRESHOLD) {
            isTabsVisible = false
        } else if (totalOffsetChange < -SCROLL_DELTA_THRESHOLD) {
            isTabsVisible = true
        }
        previousScrollIndex = currentIndex
        previousScrollOffset = currentOffset
    }

    data class Tab(
        val type: TabTypeListing,
        val title: String,
        val icon: DrawableResource? = null,
        val onClick: () -> Unit
    )

    val tabs = listOf(
        Tab(
            type = TabTypeListing.ALL,
            title = stringResource(strings.allOffers),
            onClick = {
                selectedTab = TabTypeListing.ALL
            }
        ),
        Tab(
            type = TabTypeListing.AUCTION,
            title = stringResource(strings.ordinaryAuction),
            onClick = {
                selectedTab = TabTypeListing.AUCTION
            }
        ),
        Tab(
            type = TabTypeListing.BUY_NOW,
            title = stringResource(strings.buyNow),
            onClick = {
                selectedTab = TabTypeListing.BUY_NOW
            }
        ),
    )


    offers?.loadState?.apply {
        when {
            refresh is LoadStateNotLoading && offers.itemCount < 1 -> {
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
                            (offers.loadState.refresh as LoadStateError).error.message ?: "", ""
                        )
                    ) { offers.retry() }
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
            AnimatedVisibility(
                visible = isTabsVisible,
                enter = fadeIn() ,
                exit = fadeOut() ,
                modifier = Modifier.animateContentSize()
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs) { tab ->
                        FilterChip(
                            modifier = modifier.padding(horizontal = dimens.extraSmallPadding),
                            selected = tab.type == selectedTab,
                            onClick = {tab.onClick() },
                            label = {
                                Text(tab.title)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.white,
                                labelColor = colors.black,
                                selectedContainerColor = colors.selected,
                                selectedLabelColor = colors.black
                            )
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listingData.value.filters
                    if (filters != null) {
                        items(filters.toList ()) { filter ->

                        }
                    }

                    if (searchData.value.fromSearch) {
                        if (searchData.value.searchChoice == "user_search" || searchData.value.userID != 1L) {
                            item {

                            }
                        }

                        if (searchData.value.searchString?.isNotEmpty() == true){
                            item {
                                FilterChip(
                                    modifier = modifier.padding(horizontal = dimens.extraSmallPadding),
                                    selected = false,
                                    onClick = { },
                                    label = {
                                        Row(
                                            modifier = Modifier.wrapContentSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                searchData.value.searchString!!,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        SmallIconButton(
                                            drawables.cancelIcon,
                                            color = colors.black,
                                            modifierIconSize = modifier.size(dimens.extraSmallIconSize),
                                            modifier = modifier
                                        ) {
                                            searchData.value.searchString = ""
                                            searchData.value.fromSearch = false
                                            component.onRefresh()
                                        }
                                    },
                                    border = null,
                                    shape = MaterialTheme.shapes.medium,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = colors.white,
                                        labelColor = colors.black,
                                        selectedContainerColor = colors.selected,
                                        selectedLabelColor = colors.black
                                    )
                                )
                            }
                        }
                    }
                }
                val itemFilter = NavigationItem(
                    title = stringResource(strings.filter),
                    icon = drawables.filterIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = 5
                )

                val itemSort = NavigationItem(
                    title = stringResource(strings.sort),
                    icon = drawables.sortIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null
                )
                Row(
                    modifier = Modifier,
                ) {
                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {
                            {  }
                        }
                    ) {
                        getBadgedBox(item = itemFilter)
                    }

                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {
                            {  }
                        }
                    ) {
                        getBadgedBox(item = itemSort)
                    }
                }
            }


            Box(modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
            ) {
                if (offers != null) {
                    val data by rememberUpdatedState(offers)
                    PagingGrid(
                        state = scrollState,
                        data = data,
                        content = { offer ->
                            PromoLotItem(offer) {

                            }
                        }
                    )
                }
            }
        }
    }
}
