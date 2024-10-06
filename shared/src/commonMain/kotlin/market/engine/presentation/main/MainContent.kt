package market.engine.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.NavigationItem
import market.engine.core.navigation.configs.MainConfig
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.basket.BasketNavigation
import market.engine.presentation.category.CategoryNavigation
import market.engine.presentation.favorites.FavoritesNavigation
import market.engine.presentation.home.DrawerContent
import market.engine.presentation.home.HomeNavigation
import market.engine.presentation.profile.ProfileNavigation
import market.engine.widgets.bars.getBottomNavBar
import market.engine.widgets.bars.getRailNavBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainContent(
    component: MainComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childStack.subscribeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    val currentScreen = when (childStack.active.instance) {
        is MainComponent.Child.HomeChild -> 0
        is MainComponent.Child.CategoryChild -> 1
        is MainComponent.Child.BasketChild -> 2
        is MainComponent.Child.FavoritesChild -> 3
        is MainComponent.Child.ProfileChild -> 4
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
    ){
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerContent = { DrawerContent(drawerState, scope, modifier, component::goToLogin) },
            gesturesEnabled = drawerState.isOpen,
        ){
            Scaffold(
                bottomBar = {
                    if (!showNavigationRail) {
                        getBottomNavBar(component, modifier,listItems, currentScreen)
                    } else {
                        getRailNavBar(component, modifier, currentScreen, listItems) { openMenu() }
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
                    when (child.instance) {
                        is MainComponent.Child.HomeChild ->
                            HomeNavigation(modifier, component.childHomeStack) { openMenu() }
                        is MainComponent.Child.CategoryChild ->
                            CategoryNavigation(modifier, component.childCategoryStack)
                        is MainComponent.Child.BasketChild ->
                            BasketNavigation(modifier, component.childBasketStack)
                        is MainComponent.Child.FavoritesChild ->
                            FavoritesNavigation(modifier, component.childFavoritesStack)
                        is MainComponent.Child.ProfileChild ->
                            ProfileNavigation(modifier, component.childProfileStack)
                    }
                }
            }
        }
    }
}

fun navigateFromBottomBar(index: Int, component: MainComponent){
    when(index){
        0 -> component.navigateToBottomItem(MainConfig.Home)
        1 -> component.navigateToBottomItem(MainConfig.Category)
        2 -> component.navigateToBottomItem(MainConfig.Basket)
        3 -> component.navigateToBottomItem(MainConfig.Favorites)
        4 -> component.navigateToBottomItem(MainConfig.Profile)
    }
}





