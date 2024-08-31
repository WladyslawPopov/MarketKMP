package market.engine.root

import agora.shared.generated.resources.Res
import agora.shared.generated.resources.home_icon_new
import agora.shared.generated.resources.ic_baseline_search_33
import agora.shared.generated.resources.title_home
import agora.shared.generated.resources.title_search
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BadgedBox
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
import application.market.auction_mobile.root.Config
import application.market.auction_mobile.root.RootComponent
import application.market.auction_mobile.ui.home.HomeContent
import application.market.auction_mobile.ui.search.SearchContent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.theme.Colors
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
    colors: Colors
) {
    val childStack by component.childStack.subscribeAsState()

    val listItems = listOf(
        BottomNavigationItem(
            title = stringResource(Res.string.title_home),
            selectedIcon =  Res.drawable.home_icon_new,
            unselectedIcon = Res.drawable.home_icon_new,
            hasNews = false,
            badgeCount = null
        ),
        BottomNavigationItem(
            title = stringResource(Res.string.title_search),
            selectedIcon = Res.drawable.ic_baseline_search_33,
            unselectedIcon = Res.drawable.ic_baseline_search_33,
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
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),

                contentColor = colors.errorLayoutBackground

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
                                        tint = colors.inactiveBottomNavIconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(item.unselectedIcon),
                                        contentDescription = null,
                                        tint = colors.black,
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
                is RootComponent.Child.HomeChild -> HomeContent(screen.component)
                is RootComponent.Child.SearchChild -> SearchContent(screen.component)
            }
        }
    }
}
