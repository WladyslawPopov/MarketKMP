package market.engine.core.navigation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.core.navigation.main.configs.MainConfig
import market.engine.core.navigation.main.children.BasketNavigation
import market.engine.core.navigation.main.children.FavoritesNavigation
import market.engine.core.navigation.main.children.HomeNavigation
import market.engine.core.navigation.main.children.SearchNavigation
import market.engine.core.navigation.main.children.ProfileNavigation
import market.engine.widgets.bars.getBottomNavBar


sealed class ChildMain {
    data object HomeChildMain : ChildMain()
    data object CategoryChildMain : ChildMain()
    data object BasketChildMain : ChildMain()
    data object FavoritesChildMain : ChildMain()
    data object ProfileChildMain : ChildMain()
}

@Composable
fun MainNavigation(
    component: MainComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childMainStack.subscribeAsState()

    val currentScreen = when (childStack.active.instance) {
        is ChildMain.HomeChildMain -> 0
        is ChildMain.CategoryChildMain -> 1
        is ChildMain.BasketChildMain -> 2
        is ChildMain.FavoritesChildMain -> 3
        is ChildMain.ProfileChildMain -> 4
    }

    val userInfo = UserData.userInfo

    val listItems = listOf(
        NavigationItem(
            title = strings.homeTitle,
            icon =  drawables.home,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.searchTitle,
            icon = drawables.search,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.basketTitle,
            icon = drawables.basketIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = userInfo?.countOffersInCart
        ),
        NavigationItem(
            title = strings.favoritesTitle,
            icon = drawables.favoritesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = userInfo?.countWatchedOffers
        ),
        NavigationItem(
            title = strings.profileTitleBottom,
            icon = drawables.profileIcon,
            image = userInfo?.avatar?.thumb?.content,
            tint = colors.black,
            hasNews = (
                        (userInfo?.countUnreadMessages ?: 0) > 0 ||
                        (userInfo?.countUnreadPriceProposals ?:0) > 0
                    ),
            badgeCount = null
        )
    )

    Scaffold(
        bottomBar = { getBottomNavBar(component, modifier, listItems, currentScreen) },
    ) { innerPadding ->
        Children(
            stack = childStack,
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            animation = stackAnimation(fade())
        ) { child ->
            when (child.instance) {
                is ChildMain.HomeChildMain ->
                    HomeNavigation(modifier, component.childHomeStack)

                is ChildMain.CategoryChildMain ->
                    SearchNavigation(modifier, component.childSearchStack)

                is ChildMain.BasketChildMain ->
                    BasketNavigation(modifier, component.childBasketStack)

                is ChildMain.FavoritesChildMain ->
                    FavoritesNavigation(modifier, component.childFavoritesStack)

                is ChildMain.ProfileChildMain ->
                    ProfileNavigation(modifier, component.childProfileStack)
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
