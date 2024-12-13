package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.items.NavigationItem
import market.engine.core.navigation.main.MainComponent
import market.engine.core.navigation.main.navigateFromBottomBar
import market.engine.widgets.badges.getBadgedBox
import org.jetbrains.compose.resources.stringResource

@Composable
fun getBottomNavBar(
    component: MainComponent,
    modifier: Modifier = Modifier,
    listItems: List<NavigationItem>,
    currentScreen: Int
){
    NavigationBar(
        modifier = Modifier,
    ) {
        Row(
            modifier = Modifier.background(colors.white)
        ) {
            listItems.forEachIndexed { index, item ->
                val isSelected = currentScreen == index
                NavigationBarItem(
                    colors = colors.navItemColors,
                    selected = isSelected,
                    onClick = {
                        navigateFromBottomBar(index, component)
                    },
                    icon = {
                        getBadgedBox(modifier, item, isSelected)
                    },
                    label = {
                        if(isSelected) {
                            Text(
                                text = stringResource(item.title),
                                fontSize = 9.sp,
                                maxLines = 1,
                                lineHeight = 8.sp
                            )
                        }
                    }
                )
            }
        }
    }
}


