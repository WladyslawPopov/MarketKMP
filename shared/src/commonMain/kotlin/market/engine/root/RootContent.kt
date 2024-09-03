package market.engine.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.ui.home.HomeContent
import application.market.auction_mobile.ui.search.SearchContent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass
import market.engine.theme.ThemeResources
import market.engine.widgets.DrawerContent
import market.engine.widgets.appbars.HomeAppBar
import market.engine.widgets.getBottomNavBar
import market.engine.widgets.getRailNavBar
import org.jetbrains.compose.resources.DrawableResource
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    val currentScreen = when (childStack.active.instance) {
        is RootComponent.Child.HomeChild -> "Home"
        is RootComponent.Child.SearchChild -> "Search"
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContent() },
        ){
        Scaffold(
            topBar = {
                when(currentScreen){
                    "Home" -> HomeAppBar(modifier,showNavigationRail,scope,drawerState,themeResources)
                    "Search" -> {}
                }
            },
            bottomBar = {
                if (!showNavigationRail) {
                    getBottomNavBar(component, modifier, listItems, themeResources)
                } else {
                   getRailNavBar(component, modifier, scope, drawerState, listItems, themeResources)
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Children(
                stack = childStack,
                modifier = modifier.then(
                        if (showNavigationRail) modifier.padding(start = 82.dp, top = 70.dp)
                        else modifier.padding(innerPadding))
                    .fillMaxSize(),
                animation = stackAnimation(fade())
            ) { child ->
                when (val screen = child.instance) {
                    is RootComponent.Child.HomeChild -> HomeContent(
                        screen.component,
                        modifier,
                        themeResources
                    )
                    is RootComponent.Child.SearchChild -> SearchContent(screen.component)
                }
            }
        }
    }
}



