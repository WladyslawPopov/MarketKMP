package market.engine.fragments.root.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.Serializable
import market.engine.core.data.compositions.LocalBottomBarHeight
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.basket.BasketNavigation
import market.engine.fragments.root.main.home.HomeNavigation
import market.engine.fragments.root.main.listing.SearchNavigation
import market.engine.fragments.root.main.favPages.FavoritesNavigation
import market.engine.fragments.root.main.profile.ProfileNavigation
import market.engine.widgets.bars.GetBottomNavBar
import market.engine.widgets.bars.getRailNavBar
import market.engine.widgets.dialogs.LogoutDialog


sealed class ChildMain {
    data object HomeChildMain : ChildMain()
    data object CategoryChildMain : ChildMain()
    data object BasketChildMain : ChildMain()
    data object FavoritesChildMain : ChildMain()
    data object ProfileChildMain : ChildMain()
}
const val NAVIGATION_DEBOUNCE_DELAY_MS = 100L

@Composable
fun MainNavigation(
    component: MainComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childMainStack.subscribeAsState()

    val currentScreen = remember(childStack.active.instance) { when (childStack.active.instance) {
        is ChildMain.HomeChildMain -> 0
        is ChildMain.CategoryChildMain -> 1
        is ChildMain.BasketChildMain -> 2
        is ChildMain.FavoritesChildMain -> 3
        is ChildMain.ProfileChildMain -> 4
    } }

    val model = component.model.subscribeAsState()
    val viewModel = model.value.viewModel
    val showLogoutDialog = viewModel.showLogoutDialog.collectAsState()
    var bottomBarHeight by remember { mutableStateOf(dimens.zero) }
    val density = LocalDensity.current

    Scaffold { contentPadding ->
        CompositionLocalProvider(LocalBottomBarHeight provides bottomBarHeight) {
            Children(
                stack = childStack,
                animation = stackAnimation(fade())
            ) { child ->
                Box(modifier = modifier) {
                    Row {
                        if (!model.value.showBottomBar.value) {
                            getRailNavBar(
                                listItems = model.value.bottomList.value,
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
                                    model.value.publicProfileNavigationItems.value
                                )
                        }
                    }

                    LogoutDialog(
                        showLogoutDialog = showLogoutDialog.value,
                        onDismiss = { viewModel.setLogoutDialog(false) },
                        goToLogin = {
                            viewModel.setLogoutDialog(false)
                            goToLogin(true)
                        }
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(300f)
                            .onSizeChanged {
                                bottomBarHeight = with(density) { it.height.toDp() }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GetBottomNavBar(model.value.bottomList.value, currentScreen)
                    }
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
