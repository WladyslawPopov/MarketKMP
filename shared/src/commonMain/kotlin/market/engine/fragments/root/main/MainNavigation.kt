package market.engine.fragments.root.main

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
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.root.main.basket.BasketNavigation
import market.engine.fragments.root.main.home.HomeNavigation
import market.engine.fragments.root.main.profile.navigation.ProfileNavigation
import market.engine.fragments.root.main.listing.SearchNavigation
import market.engine.fragments.root.main.favPages.FavoritesNavigation
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
            badgeCount = null,
            onClick = {
                component.navigateToBottomItem(MainConfig.Home)
            }
        ),
        NavigationItem(
            title = strings.searchTitle,
            icon = drawables.search,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                component.navigateToBottomItem(MainConfig.Search)
            }
        ),
        NavigationItem(
            title = strings.basketTitle,
            icon = drawables.basketIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countOffersInCart ?: 0) > 0) userInfo?.countOffersInCart else null,
            onClick = {
                component.navigateToBottomItem(MainConfig.Basket)
            }
        ),
        NavigationItem(
            title = strings.favoritesTitle,
            icon = drawables.favoritesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countWatchedOffers?:0) > 0) userInfo?.countWatchedOffers else null,
            onClick = {
                component.navigateToBottomItem(MainConfig.Favorites)
            }
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
            badgeCount = null,
            onClick = {
                component.navigateToBottomItem(MainConfig.Profile)
            }
        )
    )


    Scaffold(
        bottomBar = { getBottomNavBar(modifier, listItems, currentScreen) },
    ) { innerPadding ->
        Children(
            stack = childStack,
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            animation = stackAnimation(fade())
        ) { child ->
            when (val screen = child.instance) {
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

@Serializable
sealed class MainConfig {
    @Serializable
    data object Home : MainConfig()
    @Serializable
    data object Search : MainConfig()
    @Serializable
    data object Basket : MainConfig()
    @Serializable
    data object Favorites : MainConfig()
    @Serializable
    data object Profile : MainConfig()
}
