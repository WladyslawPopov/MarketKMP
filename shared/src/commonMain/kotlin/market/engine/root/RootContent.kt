package market.engine.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Surface
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass
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

data class DrawerItem(
    val title : String,
    val selectedIcon : DrawableResource,
    val unselectedIcon : DrawableResource,
    val hasNews : Boolean,
    val badgeCount : Int? = null
)

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childStack.subscribeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    val currentScreen = when (childStack.active.instance) {
        is RootComponent.Child.HomeChild -> "Home"
        is RootComponent.Child.SearchChild -> "Search"
    }
    
    val listItems = listOf(
        BottomNavigationItem(
            title = stringResource(strings.homeTitle),
            selectedIcon =  drawables.home,
            unselectedIcon = drawables.home,
            hasNews = false,
            badgeCount = null
        ),
        BottomNavigationItem(
            title = stringResource(strings.searchTitle),
            selectedIcon = drawables.search,
            unselectedIcon = drawables.search,
            hasNews = false,
            badgeCount = null
        )
    )
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.backgroundColor
    ){
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { DrawerContent(drawerState, scope) },
            gesturesEnabled = drawerState.isOpen
        ){
            Scaffold(
                topBar = {
                    when(currentScreen){
                        "Home" -> HomeAppBar(modifier,showNavigationRail,scope,drawerState)
                        "Search" -> {}
                    }
                },
                bottomBar = {
                    if (!showNavigationRail) {
                        getBottomNavBar(component, modifier,listItems)
                    } else {
                        getRailNavBar(component, modifier, scope, drawerState, listItems)
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
                            modifier
                        )
                        is RootComponent.Child.SearchChild -> SearchContent(screen.component)
                    }
                }
            }
        }
    }

}

fun navigateFromBottomBar(index: Int, component: RootComponent){
    when(index){
        0 -> component.navigateTo(Config.Home)
        1 -> component.navigateTo(Config.Search(itemId = 1))
    }
}



