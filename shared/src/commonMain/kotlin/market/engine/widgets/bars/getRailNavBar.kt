package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItemUI
import market.engine.widgets.badges.BadgedButton

@Composable
fun RailNavBar(
    modifier: Modifier = Modifier,
    currentScreen: Int,
    listItems: List<NavigationItemUI>,
){
    NavigationRail(
        modifier = modifier
            .fillMaxHeight().background(color = colors.white).offset(
                x = (-1).dp
            ),
        header = {
//            floatingCreateOfferButton{
//
//            }
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
                        Text(
                            text = item.data.title,
                            fontSize = dimens.smallText,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    }
}
