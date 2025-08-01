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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import kotlinx.serialization.Serializable
import market.engine.common.backAnimation
import market.engine.core.data.compositions.LocalBottomBarHeight
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItemUI
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.basket.BasketNavigation
import market.engine.fragments.root.main.home.HomeNavigation
import market.engine.fragments.root.main.listing.SearchNavigation
import market.engine.fragments.root.main.favPages.FavoritesNavigation
import market.engine.fragments.root.main.profile.ProfileConfig
import market.engine.fragments.root.main.profile.ProfileNavigation
import market.engine.widgets.bars.GetBottomNavBar
import market.engine.widgets.bars.RailNavBar
import market.engine.widgets.dialogs.LogoutDialog
import org.jetbrains.compose.resources.stringResource


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
    val showLogoutDialog = viewModel.showLogoutDialog.collectAsState()
    val showBottomBar = viewModel.showBottomBar.collectAsState()

    val publicProfileNavigationItemsState = viewModel.publicProfileNavigationItems.collectAsState()

    val createNewOfferTitle = stringResource(strings.createNewOfferTitle)
    val myBidsTitle = stringResource(strings.myBidsTitle)
    val proposalTitle = stringResource(strings.proposalTitle)
    val myPurchasesTitle = stringResource(strings.myPurchasesTitle)
    val myOffersTitle = stringResource(strings.myOffersTitle)
    val mySalesTitle = stringResource(strings.mySalesTitle)
    val messageTitle = stringResource(strings.messageTitle)
    val myProfileTitle = stringResource(strings.myProfileTitle)
    val settingsProfileTitle = stringResource(strings.settingsProfileTitle)
    val myBalanceTitle = stringResource(strings.myBalanceTitle)
    val logoutTitle = stringResource(strings.logoutTitle)

    val publicProfileNavigationItems = publicProfileNavigationItemsState.value.map {
        NavigationItemUI(
            data = it,
            icon = when (it.title) {
                createNewOfferTitle -> drawables.newLotIcon
                myBidsTitle -> drawables.bidsIcon
                proposalTitle -> drawables.proposalIcon
                myPurchasesTitle -> drawables.purchasesIcon
                myOffersTitle -> drawables.tagIcon
                mySalesTitle -> drawables.salesIcon
                messageTitle -> drawables.dialogIcon
                myProfileTitle -> drawables.profileIcon
                settingsProfileTitle -> drawables.settingsIcon
                myBalanceTitle -> drawables.balanceIcon
                logoutTitle -> drawables.logoutIcon
                else -> {
                    null
                }
            },
            tint = when(it.title){
                createNewOfferTitle -> colors.actionItemColors
                myBidsTitle -> colors.actionItemColors
                else -> colors.black
            },
            onClick = {
                val profileNavigation = component.modelNavigation.value.profileNavigation
                when (it.title) {
                    createNewOfferTitle -> profileNavigation.pushNew(
                        ProfileConfig.CreateOfferScreen(
                            null,
                            null,
                            CreateOfferType.CREATE,
                            null
                        )
                    )
                    myBidsTitle -> profileNavigation.replaceCurrent(
                        ProfileConfig.MyBidsScreen
                    )
                    proposalTitle -> profileNavigation.replaceCurrent(
                        ProfileConfig.MyProposalsScreen
                    )
                    myPurchasesTitle -> profileNavigation.replaceCurrent(
                        ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY)
                    )
                    myOffersTitle -> profileNavigation.replaceCurrent(
                        ProfileConfig.MyOffersScreen
                    )
                    mySalesTitle -> profileNavigation.replaceCurrent(
                        ProfileConfig.MyOrdersScreen(DealTypeGroup.SELL)
                    )
                    messageTitle -> profileNavigation.replaceCurrent(
                        ProfileConfig.ConversationsScreen()
                    )
                    myProfileTitle -> profileNavigation.pushNew(
                        ProfileConfig.UserScreen(
                            UserData.login,
                            getCurrentDate(),
                            false
                        )
                    )
                    settingsProfileTitle -> profileNavigation.replaceCurrent(
                        ProfileConfig.ProfileSettingsScreen
                    )
                    logoutTitle -> viewModel.setLogoutDialog(true)
                }
            }
        )
    }
    val bottomListState = viewModel.bottomList.collectAsState()
    val homeTitle = stringResource(strings.homeTitle)
    val searchTitle = stringResource(strings.searchTitle)
    val basketTitle = stringResource(strings.basketTitle)
    val favoritesTitle = stringResource(strings.favoritesTitle)
    val profileTitleBottom = stringResource(strings.profileTitleBottom)

    val bottomList = bottomListState.value.map {
        NavigationItemUI(
            data = it,
            icon = when (it.title) {
                homeTitle -> drawables.home
                searchTitle -> drawables.search
                basketTitle -> drawables.basketIcon
                favoritesTitle -> drawables.favoritesIcon
                profileTitleBottom -> drawables.profileIcon
                else -> {
                    drawables.infoIcon
                }
            },
            tint = colors.black,
            onClick = {
                when (it.title) {
                    homeTitle -> viewModel.debouncedNavigate(MainConfig.Home)
                    searchTitle -> viewModel.debouncedNavigate(MainConfig.Search)
                    basketTitle -> viewModel.debouncedNavigate(MainConfig.Basket)
                    favoritesTitle -> viewModel.debouncedNavigate(MainConfig.Favorites)
                    profileTitleBottom -> viewModel.debouncedNavigate(MainConfig.Profile)
                }
            }
        )
    }

    var bottomBarHeight by mutableStateOf(LocalBottomBarHeight.current)
    val density = LocalDensity.current

    Scaffold {
        CompositionLocalProvider(LocalBottomBarHeight provides bottomBarHeight) {
            Children(
                modifier = modifier,
                stack = childStack,
                animation = backAnimation(
                    backHandler = component.model.value.backHandler,
                    onBack = {}
                ),
            ) { child ->
                Box {
                    Row {
                        if (!showBottomBar.value) {
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
                                    publicProfileNavigationItems
                                )
                        }
                    }

                    if (showBottomBar.value) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(300f)
                                .onSizeChanged {
                                    bottomBarHeight = with(density) {
                                        it.height.toDp()
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GetBottomNavBar(bottomList, currentScreen)
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
