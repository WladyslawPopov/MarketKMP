package market.engine.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
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
import market.engine.ui.search.SearchContent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.items.NavigationItem
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass
import market.engine.ui.basket.BasketContent
import market.engine.ui.favorites.FavoritesContent
import market.engine.ui.listing.ListingContent
import market.engine.ui.profile.ProfileContent
import market.engine.widgets.pages.DrawerContent
import market.engine.widgets.bottombars.getBottomNavBar
import market.engine.widgets.bottombars.getRailNavBar
import org.jetbrains.compose.resources.stringResource

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
        is RootComponent.Child.HomeChild -> 0
        is RootComponent.Child.SearchChild -> 1
        is RootComponent.Child.BasketChild -> 2
        is RootComponent.Child.FavoritesChild -> 3
        is RootComponent.Child.ProfileChild -> 4
        is RootComponent.Child.ListingChild -> 5
    }
    
    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.homeTitle),
            icon =  drawables.home,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.search,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.basketTitle),
            icon = drawables.basketIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = 6
        ),
        NavigationItem(
            title = stringResource(strings.favoritesTitle),
            icon = drawables.favoritesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = 687
        ),
        NavigationItem(
            title = stringResource(strings.profileTitleBottom),
            icon = drawables.profileIcon,
            tint = colors.black,
            hasNews = true,
            badgeCount = null
        )
    )

    fun openMenu(){
        scope.launch {
            drawerState.open()
        }
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.backgroundColor
    ){
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerContent = { DrawerContent(drawerState, scope, modifier) },
            gesturesEnabled = drawerState.isOpen,
        ){
            Scaffold(
                bottomBar = {
                    if (!showNavigationRail) {
                        getBottomNavBar(component, modifier,listItems, currentScreen)
                    } else {
                        getRailNavBar(component, modifier, listItems) { openMenu() }
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { innerPadding ->
                Children(
                    stack = childStack,
                    modifier = modifier.then(
                        if (showNavigationRail) modifier.padding(start = 82.dp)
                        else modifier.padding(innerPadding))
                        .fillMaxSize(),
                    animation = stackAnimation(fade())
                ) { child ->
                    when (val screen = child.instance) {
                        is RootComponent.Child.HomeChild -> HomeContent(
                            screen.component,
                            modifier
                        ) { openMenu() }

                        is RootComponent.Child.SearchChild -> SearchContent(screen.component, modifier)
                        is RootComponent.Child.BasketChild -> BasketContent(screen.component)
                        is RootComponent.Child.FavoritesChild -> FavoritesContent(screen.component)
                        is RootComponent.Child.ProfileChild -> ProfileContent(screen.component)
                        is RootComponent.Child.ListingChild -> ListingContent(screen.component, modifier)
                    }
                }
            }
        }
    }
}

fun navigateFromBottomBar(index: Int, component: RootComponent){
    when(index){
        0 -> component.navigateToBottomItem(Config.Home)
        1 -> component.navigateToBottomItem(Config.Search)
        2 -> component.navigateToBottomItem(Config.Basket)
        3 -> component.navigateToBottomItem(Config.Favorites)
        4 -> component.navigateToBottomItem(Config.Profile)
    }
}





