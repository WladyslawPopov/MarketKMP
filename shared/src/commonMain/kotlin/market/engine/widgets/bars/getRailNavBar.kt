package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.floatingCreateOfferButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun getRailNavBar(
    modifier: Modifier = Modifier,
    currentScreen: Int,
    listItems: List<NavigationItem>,
    openMenu : () -> Unit,
){
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight().background(color = colors.white).offset(
                x = (-1).dp
            ),
        header = {
            when (currentScreen){
                0 -> {
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
                }
                4 -> {
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
                }
                else -> {

                }
            }

            floatingCreateOfferButton{

            }
        },
        containerColor = colors.white,
        contentColor = colors.lightGray
    ){
        listItems.forEachIndexed { index, item ->
            val isSelected = currentScreen == index
            NavigationRailItem(
                colors = colors.navRailItemColors,
                selected = isSelected,
                onClick = item.onClick,
                icon = {
                    BadgedButton(item, isSelected)
                },
                label = {
                    if(isSelected) {
                        Text(text = item.title, fontSize = 10.sp)
                    }
                }
            )
        }
    }
}
