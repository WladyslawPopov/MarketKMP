package market.engine.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import market.engine.root.BottomNavigationItem
import market.engine.root.Config
import market.engine.root.RootComponent
import market.engine.theme.ThemeResources
import org.jetbrains.compose.resources.painterResource

@Composable
fun getRailNavBar(
    component: RootComponent,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    drawerState: DrawerState,
    listItems: List<BottomNavigationItem>,
    themeResources: ThemeResources
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
            Icon(
                painter = painterResource(themeResources.drawables.menuHamburger),
                contentDescription = "Menu",
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .clickable {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                tint = themeResources.colors.white
            )

            FloatingActionButton(
                contentColor = themeResources.colors.white,
                containerColor = themeResources.colors.white,
                onClick = { },
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),

                ){
                Icon(
                    tint = themeResources.colors.inactiveBottomNavIconColor,
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    ){
        listItems.forEachIndexed { index, item ->
            NavigationRailItem(
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
