package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.Tab
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.tabs.ReorderTabRow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavPagesAppBar(
    currentTab : Int,
    favTabList: List<FavoriteListItem>,
    modifier: Modifier = Modifier,
    isDragMode: MutableState<Boolean>,
    navigationClick : (Int) -> Unit,
    onTabsReordered: (List<FavoriteListItem>) -> Unit,
    onRefresh: () -> Unit
) {
    val listItems = listOf(
        NavigationItem(
            title = "",
            icon = drawables.recycleIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
            badgeCount = null,
            onClick = onRefresh
        ),
        NavigationItem(
            title = stringResource(strings.createNewOffersListLabel),
            icon = drawables.newLotIcon,
            tint = colors.steelBlue,
            hasNews = false,
            isVisible = !isDragMode.value,
            badgeCount = null,
            onClick = onRefresh
        ),
        NavigationItem(
            title = stringResource(strings.menuTitle),
            icon = drawables.menuIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            isVisible = favTabList.isNotEmpty() && favTabList[currentTab].id > 1000 && !isDragMode.value,
            badgeCount = null,
            onClick = {

            }
        ),
    )

    val lazyListState = rememberLazyListState()

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ),
        modifier = modifier,
        title = {
            if (favTabList.isNotEmpty()) {
                ReorderTabRow(
                    tabs = favTabList.map {
                        Tab(
                            title = it.title ?: "",
                            image = it.images.firstOrNull(),
                        )
                    }.toList(),
                    selectedTab = currentTab,
                    onTabSelected = {
                        navigationClick(it)
                    },
                    isDragMode = isDragMode,
                    onTabsReordered = { list ->
                        val newList = list.map { listItem ->
                            favTabList.find { it.title == listItem.title} ?: FavoriteListItem()
                        }
                        onTabsReordered(newList)
                    },
                    lazyListState = lazyListState,
                    modifier = Modifier.fillMaxWidth().padding(end = dimens.smallPadding),
                )
            }
        },
        actions = {
            Column {
                Row(
                    modifier = Modifier.padding(end = dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        alignment = Alignment.End
                    )
                ) {
                    listItems.forEachIndexed { _, item ->
                        if(item.isVisible){
                            BadgedButton(item)
                        }
                    }
                }
            }
        }
    )
}
