package market.engine.widgets

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.root.BottomNavigationItem
import market.engine.root.Config
import market.engine.root.RootComponent
import market.engine.theme.ThemeResources
import org.jetbrains.compose.resources.painterResource

@Composable
fun getBottomNavBar(
    component: RootComponent,
    modifier: Modifier = Modifier,
    listItems: List<BottomNavigationItem>,
    themeResources: ThemeResources
){
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }
    
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = themeResources.dimens.smallPadding, topEnd = themeResources.dimens.smallPadding)),
        
        contentColor = themeResources.colors.errorLayoutBackground
    ) {
        listItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    when(index){
                        0 -> component.navigateTo(Config.Home)
                        1 -> component.navigateTo(Config.Search(itemId = 1))
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount != null){
                                Badge {
                                    Text(text = item.badgeCount.toString())
                                }
                            } else {
                                if (item.hasNews) {
                                    Badge()
                                }
                            }
                        }
                    ){
                        if (selectedItemIndex == index){
                            Icon(
                                painter = painterResource(item.selectedIcon),
                                contentDescription = item.title,
                                tint = themeResources.colors.inactiveBottomNavIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(item.unselectedIcon),
                                contentDescription = null,
                                tint = themeResources.colors.black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(text = item.title)
                }
            )
        }
    }
}
