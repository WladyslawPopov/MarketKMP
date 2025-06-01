package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.Tab
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Operations
import market.engine.core.repositories.SettingsRepository
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.tabs.ReorderTabRow
import market.engine.widgets.tooltip.TooltipState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavPagesAppBar(
    currentTab : Int,
    settings: SettingsRepository,
    favTabList: List<FavoriteListItem>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    tooltipState: TooltipState,
    isDragMode: Boolean,
    navigationClick : (Int) -> Unit,
    onTabsReordered: (List<FavoriteListItem>) -> Unit,
    getOperations: (Long, (List<Operations>) -> Unit) -> Unit,
    makeOperation: (String, Long) -> Unit,
    onRefresh: () -> Unit
) {
    val openPopup = remember { mutableStateOf(false) }
    val defReorderMenuItem = MenuItem(
        icon = drawables.reorderIcon,
        id = "reorder",
        title = stringResource(strings.reorderTabLabel),
        onClick = {
            makeOperation("reorder", 1L)
        }
    )

    val menuList = remember {
        mutableStateOf(
            listOf(
                defReorderMenuItem
            )
        )
    }

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
            icon = drawables.addFolderIcon,
            tint = colors.steelBlue,
            hasNews = settings.getSettingValue("create_blank_offer_list_notify_badge", true) == true,
            isVisible = !isDragMode,
            badgeCount = null,
            onClick = {
                settings.setSettingValue("create_blank_offer_list_notify_badge", false)
                makeOperation("create_blank_offer_list", UserData.login)
            }
        ),
        NavigationItem(
            title = stringResource(strings.menuTitle),
            icon = drawables.menuIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            isVisible = favTabList.isNotEmpty() && favTabList[currentTab].id > 1000 && !isDragMode,
            badgeCount = null,
            onClick = {
                getOperations(favTabList[currentTab].id){ operations ->
                    menuList.value = buildList {
                        addAll(listOf(
                            defReorderMenuItem
                        ))
                        addAll(
                            operations.map {
                                MenuItem(
                                    id = it.id ?: "",
                                    title = it.name ?: "",
                                    onClick = {
                                        makeOperation(it.id ?: "", favTabList[currentTab].id)
                                    }
                                )
                            }
                        )
                    }
                    openPopup.value = true
                }
            }
        ),
    )

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
                            id = it.id,
                            title = it.title ?: "",
                            image = it.images.firstOrNull(),
                            isPined = it.markedAsPrimary,
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
                    getOperations = getOperations,
                    makeOperation = makeOperation,
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
                        if (item.isVisible) {
                            BadgedButton(
                                item,
                                tooltipState = tooltipState
                            )
                        }
                    }
                }

                PopUpMenu(
                    openPopup = openPopup.value,
                    menuList = menuList.value,
                    onClosed = {
                        openPopup.value = false
                    }
                )
            }
        }
    )
}
