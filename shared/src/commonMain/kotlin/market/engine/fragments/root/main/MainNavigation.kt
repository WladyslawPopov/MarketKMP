package market.engine.fragments.root.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.basket.BasketNavigation
import market.engine.fragments.root.main.home.HomeNavigation
import market.engine.fragments.root.main.profile.navigation.ProfileNavigation
import market.engine.fragments.root.main.listing.SearchNavigation
import market.engine.fragments.root.main.favPages.FavoritesNavigation
import market.engine.widgets.bars.getBottomNavBar
import market.engine.widgets.bars.getRailNavBar
import org.jetbrains.compose.resources.stringResource


sealed class ChildMain {
    data object HomeChildMain : ChildMain()
    data object CategoryChildMain : ChildMain()
    data object BasketChildMain : ChildMain()
    data object FavoritesChildMain : ChildMain()
    data object ProfileChildMain : ChildMain()
}

private const val NAVIGATION_DEBOUNCE_DELAY_MS = 100L

@Composable
fun MainNavigation(
    component: MainComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childMainStack.subscribeAsState()

    var lastNavigationClickTime by remember { mutableStateOf(0L) }

    val debouncedNavigate: (MainConfig) -> Unit = { targetConfig ->
        val currentTime = (getCurrentDate().toLongOrNull() ?: 1L)*1000
        if (currentTime - lastNavigationClickTime > NAVIGATION_DEBOUNCE_DELAY_MS) {
            lastNavigationClickTime = currentTime
            component.navigateToBottomItem(targetConfig)
        }
    }

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
            title = stringResource(strings.homeTitle),
            icon =  drawables.home,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                debouncedNavigate(MainConfig.Home)
            }
        ),
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.search,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                debouncedNavigate(MainConfig.Search)
            }
        ),
        NavigationItem(
            title = stringResource(strings.basketTitle),
            icon = drawables.basketIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countOffersInCart ?: 0) > 0) userInfo?.countOffersInCart else null,
            onClick = {
                debouncedNavigate(MainConfig.Basket)
            }
        ),
        NavigationItem(
            title = stringResource(strings.favoritesTitle),
            icon = drawables.favoritesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countWatchedOffers?:0) > 0) userInfo?.countWatchedOffers else null,
            onClick = {
                debouncedNavigate(MainConfig.Favorites)
            }
        ),
        NavigationItem(
            title = stringResource(strings.profileTitleBottom),
            icon = drawables.profileIcon,
            imageString = userInfo?.avatar?.thumb?.content,
            tint = colors.black,
            hasNews = (
                        (userInfo?.countUnreadMessages ?: 0) > 0 ||
                        (userInfo?.countUnreadPriceProposals ?:0) > 0
                    ),
            badgeCount = null,
            onClick = {
                debouncedNavigate(MainConfig.Profile)
            }
        )
    )

    val profileNavigation = component.modelNavigation.value.profileNavigation
    val model = component.model.subscribeAsState()



    Scaffold(
        bottomBar = {  if (model.value.showBottomBar.value){ getBottomNavBar(listItems, currentScreen) }},
    ) { innerPadding ->
        Children(
            stack = childStack,
            modifier = modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            animation = stackAnimation(fade())
        ) { child ->
            Row {
                if (!model.value.showBottomBar.value) {
                    getRailNavBar(listItems = listItems, currentScreen = currentScreen)
                }
                when (child.instance) {
                    is ChildMain.HomeChildMain ->
                        HomeNavigation(Modifier.weight(1f), component.childHomeStack)

                    is ChildMain.CategoryChildMain ->
                        SearchNavigation(Modifier.weight(1f), component.childSearchStack)

                    is ChildMain.BasketChildMain ->
                        BasketNavigation(Modifier.weight(1f), component.childBasketStack)

                    is ChildMain.FavoritesChildMain ->
                        FavoritesNavigation(Modifier.weight(1f), component.childFavoritesStack)

                    is ChildMain.ProfileChildMain ->
                        ProfileNavigation(
                            Modifier.weight(1f),
                            component.childProfileStack,
                            profileNavigation
                        )
                }
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
