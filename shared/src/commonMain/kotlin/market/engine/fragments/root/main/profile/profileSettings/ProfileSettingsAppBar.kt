package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.Tab
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.tabs.PageTab
import market.engine.widgets.tabs.TabRow
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsAppBar(
    currentTab : Int,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    showMenu : Boolean? = null,
    openMenu : ((CoroutineScope) -> Unit)? = null,
    navigationClick : (Int) -> Unit
) {
    val tabs = listOf(
        Tab(
            title = stringResource(strings.profileGlobalSettingsLabel),
        ),
        Tab(
            title = stringResource(strings.profileSellerSettingsLabel),
        ),
        Tab(
            title = stringResource(strings.profileAdditionalSettingsLabel),
        )
    )

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.white,
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
            TabRow(
                tabs,
                selectedTab = currentTab,
                edgePadding = 0.dp,
                containerColor = colors.white,
                modifier = Modifier.fillMaxWidth(),
            ){ index, tab ->
                PageTab(
                    tab = tab,
                    selectedTab = currentTab,
                    currentIndex = index,
                    textStyle = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable {
                        navigationClick(index)
                    },
                )
            }
        },
//        actions = {
//            Column {
//                Row(
//                    modifier = modifier.padding(end = dimens.smallPadding),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(
//                        dimens.smallPadding,
//                        alignment = Alignment.End
//                    )
//                ) {
//                    listItems.forEachIndexed { _, item ->
//                        if(item.isVisible){
//                            BadgedButton(item)
//                        }
//                    }
//                }
//            }
//        }
    )
}
