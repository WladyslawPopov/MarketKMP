package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.items.NavigationItem
import market.engine.presentation.main.MainComponent
import market.engine.presentation.main.navigateFromBottomBar
import market.engine.widgets.badges.getBadgedBox

@Composable
fun getBottomNavBar(
    component: MainComponent,
    modifier: Modifier = Modifier,
    listItems: List<NavigationItem>,
    currentScreen: Int
){
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding(),
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
                                text = item.title,
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


