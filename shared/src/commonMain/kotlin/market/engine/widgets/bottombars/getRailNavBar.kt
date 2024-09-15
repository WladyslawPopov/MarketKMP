package market.engine.widgets.bottombars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.items.NavigationItem
import market.engine.root.RootComponent
import market.engine.root.navigateFromBottomBar
import market.engine.widgets.common.getBadgedBox
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun getRailNavBar(
    component: RootComponent,
    modifier: Modifier = Modifier,
    listItems: List<NavigationItem>,
    openMenu : () -> Unit,
){
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }
    
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight().background(MaterialTheme.colorScheme.inverseOnSurface).offset(
                x = (-1).dp
            ),
        header = {
            IconButton(
                modifier = modifier,
                onClick = {
                    openMenu()
                }
            ){
                Icon(
                    painter = painterResource(drawables.menuHamburger),
                    contentDescription = stringResource(strings.menuTitle),
                    modifier = modifier.size(dimens.smallIconSize),
                    tint = colors.black
                )
            }

            FloatingActionButton(
                containerColor = colors.white,
                contentColor = colors.grayLayout,
                onClick = { },
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            ){
                Icon(
                    tint = colors.inactiveBottomNavIconColor,
                    painter = painterResource(drawables.newLotIcon),
                    contentDescription = stringResource(strings.newLotTitle)
                )
            }
        },
        containerColor = colors.white,
        contentColor = colors.lightGray
    ){
        listItems.forEachIndexed { index, item ->
            NavigationRailItem(
                colors = colors.navRailItemColors,
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    navigateFromBottomBar(index, component)
                },
                icon = {
                    getBadgedBox(modifier = modifier, item,selectedItemIndex == index)
                },
                label = {
                    if(selectedItemIndex == index) {
                        Text(text = item.title, fontSize = 10.sp)
                    }
                }
            )
        }
    }
}
