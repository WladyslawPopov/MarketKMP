package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.PlatformWindowType
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrderAppBar(
    currentTab : DealType,
    typeGroup : DealTypeGroup,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    showMenu : Boolean? = null,
    openMenu : ((CoroutineScope) -> Unit)? = null,
    navigationClick : (DealType) -> Unit,
    onRefresh: () -> Unit
) {
    val tabs = when (typeGroup){
        DealTypeGroup.BUY -> {
            listOf(
                DealType.BUY_IN_WORK to strings.tabInWorkLabel,
                DealType.BUY_ARCHIVE to strings.tabArchiveLabel
            )
        }
        DealTypeGroup.SELL -> {
            listOf(
                DealType.SELL_ALL to strings.tabAllLabel,
                DealType.SELL_IN_WORK to strings.tabInWorkLabel,
                DealType.SELL_ARCHIVE to strings.tabArchiveLabel,
            )
        }
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
    )

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            MenuHamburgerButton(
                drawerState,
                showMenu = showMenu,
                openMenu = openMenu
            )
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){
                tabs.forEach { tab ->
                    item {
                        SimpleTextButton(
                            stringResource(tab.second),
                            backgroundColor = if (currentTab == tab.first) colors.rippleColor else colors.white,
                            textStyle = MaterialTheme.typography.bodyMedium,
                        ) {
                            navigationClick(tab.first)
                        }
                    }
                }
            }
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
