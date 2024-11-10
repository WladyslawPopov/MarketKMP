package market.engine.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.core.navigation.children.ChildMain
import market.engine.core.navigation.configs.MainConfig
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.BaseContent
import market.engine.presentation.basket.BasketNavigation
import market.engine.presentation.category.CategoryNavigation
import market.engine.presentation.favorites.FavoritesNavigation
import market.engine.presentation.home.DrawerContent
import market.engine.presentation.home.HomeAppBar
import market.engine.presentation.home.HomeNavigation
import market.engine.presentation.profile.ProfileNavigation
import market.engine.widgets.bars.getBottomNavBar
import market.engine.widgets.exceptions.onError
import org.jetbrains.compose.resources.stringResource


var topBar : @Composable () -> Unit = {}
var drower : @Composable () -> Unit = {}
var onRefresh : () -> Unit = {}
val scrollState : ScrollState? = null
val isShowFloatingButton : Boolean = false

val isLoading = mutableStateOf(false)

val error: (@Composable () -> Unit)? = null

@Composable
fun MainContent(
    component: MainComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childMainStack.subscribeAsState()

    val model = component.mainViewModel

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
            badgeCount = userInfo?.countOffersInCart
        ),
        NavigationItem(
            title = stringResource(strings.favoritesTitle),
            icon = drawables.favoritesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = userInfo?.countWatchedOffers
        ),
        NavigationItem(
            title = stringResource(strings.profileTitleBottom),
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


    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun openMenu(){
        scope.launch {
            drawerState.open()
        }
    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        isShowFloatingButton = isShowFloatingButton,
        showVerticalScrollbarState = scrollState,
        drawerContent = drower,
        drawerState = drawerState,
        topBar = topBar,
        bottomBar = {getBottomNavBar(component, modifier, listItems, currentScreen)},
        onRefresh = onRefresh,
        error = error
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = !isLoading.value,
            enter = expandIn(),
            exit = fadeOut()
        ) {
            Children(
                stack = childStack,
                modifier = modifier
                    .fillMaxSize(),
                animation = stackAnimation(fade())
            ) { child ->
                when (child.instance) {
                    is ChildMain.HomeChildMain ->
                        HomeNavigation(modifier, component.childHomeStack)

                    is ChildMain.CategoryChildMain ->
                        CategoryNavigation(modifier, component.childCategoryStack)

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
