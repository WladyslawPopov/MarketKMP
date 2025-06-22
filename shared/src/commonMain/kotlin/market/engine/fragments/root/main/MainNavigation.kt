package market.engine.fragments.root.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.Serializable
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.basket.BasketNavigation
import market.engine.fragments.root.main.home.HomeNavigation
import market.engine.fragments.root.main.listing.SearchNavigation
import market.engine.fragments.root.main.favPages.FavoritesNavigation
import market.engine.fragments.root.main.profile.ProfileNavigation
import market.engine.widgets.bars.getBottomNavBar
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
    val showLogoutDialog = remember { viewModel.showLogoutDialog }

    Scaffold(
        bottomBar = { if (model.value.showBottomBar.value){ getBottomNavBar(model.value.bottomList.value, currentScreen) }},
    ) { innerPadding ->
        Children(
            stack = childStack,
            modifier = modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            animation = stackAnimation(fade())
        ) { child ->
            Row {
                if (!model.value.showBottomBar.value) {
                    getRailNavBar(listItems = model.value.bottomList.value, currentScreen = currentScreen)
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
                            model.value.publicProfileNavigationItems.value
                        )
                }
            }

            LogoutDialog(
                showLogoutDialog = showLogoutDialog.value,
                onDismiss = { showLogoutDialog.value = false },
                goToLogin = {
                    showLogoutDialog.value = false
                    goToLogin(true)
                }
            )
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
