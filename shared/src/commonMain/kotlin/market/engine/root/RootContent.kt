package market.engine.root

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BadgedBox
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.ui.home.HomeContent
import application.market.auction_mobile.ui.search.SearchContent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.theme.ThemeResources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class BottomNavigationItem(
    val title : String,
    val selectedIcon : DrawableResource,
    val unselectedIcon : DrawableResource,
    val hasNews : Boolean,
    val badgeCount : Int? = null
)

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
    themeResources: ThemeResources
) {
    val childStack by component.childStack.subscribeAsState()

    val listItems = listOf(
        BottomNavigationItem(
            title = stringResource(themeResources.strings.homeTitle),
            selectedIcon =  themeResources.drawables.home,
            unselectedIcon = themeResources.drawables.home,
            hasNews = false,
            badgeCount = null
        ),
        BottomNavigationItem(
            title = stringResource(themeResources.strings.searchTitle),
            selectedIcon = themeResources.drawables.search,
            unselectedIcon = themeResources.drawables.search,
            hasNews = false,
            badgeCount = null
        )
    )

    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    Scaffold(
        bottomBar = {
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
        },

        modifier = modifier
    ) { innerPadding ->
        Children(
            stack = childStack,
            modifier = modifier.padding(innerPadding),
            animation = stackAnimation(fade())
        ) { child ->
            when (val screen = child.instance) {
                is RootComponent.Child.HomeChild -> HomeContent(screen.component, modifier, themeResources)
                is RootComponent.Child.SearchChild -> SearchContent(screen.component)
            }
        }
    }
}
