package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.PlatformWindowType
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.tabs.SimpleTabs
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavPagesAppBar(
    currentTab : FavScreenType,
    modifier: Modifier = Modifier,
    navigationClick : (FavScreenType) -> Unit,
    onRefresh: () -> Unit
) {
    val fav = stringResource(strings.myFavoritesTitle)
    val sub = stringResource(strings.mySubscribedTitle)
    val note = stringResource(strings.myNotesTitle)
    val tabList = listOf(fav, sub, note)

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
    )

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        modifier = modifier
            .fillMaxWidth(),
        title = {
            SimpleTabs(
                tabList,
                selectedTab = when(currentTab){
                    FavScreenType.FAVORITES -> 0
                    FavScreenType.SUBSCRIBED -> 1
                    FavScreenType.NOTES -> 2
                },
                onTabSelected = { index ->
                    val type = when(index) {
                        0 -> FavScreenType.FAVORITES
                        1 -> FavScreenType.SUBSCRIBED
                        2 -> FavScreenType.NOTES
                        else -> FavScreenType.FAVORITES
                    }
                    navigationClick(type)
                },
                edgePadding = dimens.smallPadding,
                containerColor = colors.transparent,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            Column {
                Row(
                    modifier = modifier.padding(end = dimens.smallPadding),
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
