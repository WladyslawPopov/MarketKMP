package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton

@Composable
fun getBottomNavBar(
    listItems: List<NavigationItem>,
    currentScreen: Int,
){
    NavigationBar {
        Row(
            modifier = Modifier.background(colors.white)
        ) {
            listItems.forEachIndexed { index, item ->
                val isSelected = currentScreen == index
                NavigationBarItem(
                    colors = colors.navItemColors,
                    selected = isSelected,
                    onClick = item.onClick,
                    icon = {
                        BadgedButton(item, isSelected)
                    },
                    label = {
                        if(isSelected) {
                            Text(
                                text = item.title,
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
}
