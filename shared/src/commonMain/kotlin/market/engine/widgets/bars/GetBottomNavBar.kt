package market.engine.widgets.bars

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.constants.alphaBars
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton

@Composable
fun GetBottomNavBar(
    listItems: List<NavigationItem>,
    currentScreen: Int,
){
    NavigationBar(
        modifier = Modifier.clip(MaterialTheme.shapes.small).fillMaxWidth(),
        containerColor = colors.white.copy(alphaBars),
        tonalElevation = dimens.smallElevation
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
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
