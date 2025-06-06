package market.engine.fragments.root.main.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.MenuHamburgerButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    drawerState: DrawerState,
    listItems: List<NavigationItem>,
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Image(
                painter = painterResource(drawables.logo),
                contentDescription = stringResource(strings.homeTitle),
                modifier = Modifier.size(140.dp, 68.dp),
            )
        },
        navigationIcon = {
            MenuHamburgerButton(
                drawerState
            )
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, alignment = Alignment.End)
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        BadgedButton(item)
                    }
                }
            }
        }
    )
}
