package market.engine.fragments.root.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.Serializable
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.DefaultMainComponent.Companion.localBottomBarHeight
import market.engine.fragments.root.main.basket.BasketNavigation
import market.engine.fragments.root.main.home.HomeNavigation
import market.engine.fragments.root.main.listing.SearchNavigation
import market.engine.fragments.root.main.favPages.FavoritesNavigation
import market.engine.fragments.root.main.profile.ProfileNavigation
import market.engine.widgets.bars.GetBottomNavBar
import market.engine.widgets.bars.RailNavBar
import market.engine.widgets.dialogs.LogoutDialog


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

    val currentScreen = remember(childStack.active.instance) {
        when (childStack.active.instance) {
            is ChildMain.HomeChildMain -> 0
            is ChildMain.CategoryChildMain -> 1
            is ChildMain.BasketChildMain -> 2
            is ChildMain.FavoritesChildMain -> 3
            is ChildMain.ProfileChildMain -> 4
        }
    }

    val model = component.model.subscribeAsState()
    val viewModel = model.value.viewModel
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()
    val showBottomBar by viewModel.showBottomBar.collectAsState()
    val bottomList by viewModel.bottomList.collectAsState()
    val profileNavigationItems by viewModel.publicProfileNavigationItems.collectAsState()
    val density = LocalDensity.current

    Scaffold {
        Children(
            modifier = modifier,
            stack = childStack,
            animation = stackAnimation(),
        ) { child ->
            Box {
                Row {
                    if (!showBottomBar) {
                        RailNavBar(
                            listItems = bottomList,
                            currentScreen = currentScreen
                        )
                    }
                    when (child.instance) {
                        is ChildMain.HomeChildMain ->
                            HomeNavigation(
                                Modifier.weight(1f),
                                component.childHomeStack
                            )

                        is ChildMain.CategoryChildMain ->
                            SearchNavigation(Modifier.weight(1f), component.childSearchStack)

                        is ChildMain.BasketChildMain ->
                            BasketNavigation(Modifier.weight(1f), component.childBasketStack)

                        is ChildMain.FavoritesChildMain ->
                            FavoritesNavigation(
                                Modifier.weight(1f),
                                component.childFavoritesStack
                            )

                        is ChildMain.ProfileChildMain ->
                            ProfileNavigation(
                                Modifier.weight(1f),
                                component.childProfileStack,
                                profileNavigationItems
                            )
                    }
                }

                if (showBottomBar) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .onSizeChanged(
                                onSizeChanged = {
                                    val newHeight = with(density) { it.height.toDp() }

                                    if(localBottomBarHeight.value < newHeight.value) {
                                        localBottomBarHeight = newHeight
                                    }
                                }
                            )
                            .zIndex(300f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GetBottomNavBar(bottomList, currentScreen)
                    }
                }

                LogoutDialog(
                    showLogoutDialog = showLogoutDialog,
                    onDismiss = { viewModel.setLogoutDialog(false) },
                    goToLogin = {
                        viewModel.setLogoutDialog(false)
                        viewModel.debouncedNavigate(MainConfig.Home)
                        goToLogin()
                    }
                )
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
