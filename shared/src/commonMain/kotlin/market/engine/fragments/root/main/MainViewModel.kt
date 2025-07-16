package market.engine.fragments.root.main

import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.Platform
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.DefaultRootComponent.Companion.goToVerification
import market.engine.fragments.root.main.home.ChildHome
import market.engine.fragments.root.main.home.HomeConfig
import market.engine.fragments.root.main.profile.ProfileConfig
import org.jetbrains.compose.resources.getString

class MainViewModel(val component: MainComponent) : CoreViewModel() {
    private val _showBottomBar = MutableStateFlow(checkShowBar())
    val showBottomBar = _showBottomBar.asStateFlow()

    private val _bottomList = MutableStateFlow(emptyList<NavigationItem>())
    val bottomList = _bottomList.asStateFlow()

    private val _publicProfileNavigationItems = MutableStateFlow(emptyList<NavigationItem>())
    val publicProfileNavigationItems = _publicProfileNavigationItems.asStateFlow()

    var lastNavigationClickTime = 0L

    init {
        viewModelScope.launch {
            snapshotFlow { UserData.userInfo }
                .collect { newInfo ->
                    updateNavLists()
                }
        }
    }

    fun checkShowBar() : Boolean {
        try {
            val platform = Platform().getPlatform()
            return !isBigScreen.value || (PlatformWindowType.MOBILE_PORTRAIT == platform || PlatformWindowType.TABLET_PORTRAIT == platform)
        }catch (_ : Exception){
            return true
        }
    }

    fun updateNavLists() {
        viewModelScope.launch {
            val userInfo = UserData.userInfo
            val profileNavigation = component.modelNavigation.value.profileNavigation

            _bottomList.value = listOf(
                NavigationItem(
                    title = getString(strings.homeTitle),
                    icon = drawables.home,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        debouncedNavigate(MainConfig.Home)
                    }
                ),
                NavigationItem(
                    title = getString(strings.searchTitle),
                    icon = drawables.search,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        debouncedNavigate(MainConfig.Search)
                    }
                ),
                NavigationItem(
                    title = getString(strings.basketTitle),
                    icon = drawables.basketIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = if ((userInfo?.countOffersInCart
                            ?: 0) > 0
                    ) userInfo?.countOffersInCart else null,
                    onClick = {
                        debouncedNavigate(MainConfig.Basket)
                    }
                ),
                NavigationItem(
                    title = getString(strings.favoritesTitle),
                    icon = drawables.favoritesIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = if ((userInfo?.countWatchedOffers
                            ?: 0) > 0
                    ) userInfo?.countWatchedOffers else null,
                    onClick = {
                        debouncedNavigate(MainConfig.Favorites)
                    }
                ),
                NavigationItem(
                    title = getString(strings.profileTitleBottom),
                    icon = drawables.profileIcon,
                    imageString = userInfo?.avatar?.thumb?.content,
                    tint = colors.black,
                    hasNews = (
                            (userInfo?.countUnreadMessages ?: 0) > 0 ||
                                    (userInfo?.countUnreadPriceProposals ?: 0) > 0
                            ),
                    badgeCount = null,
                    onClick = {
                        debouncedNavigate(MainConfig.Profile)
                    }
                )
            )
            _publicProfileNavigationItems.value = listOf(
                NavigationItem(
                    title = getString(strings.createNewOfferTitle),
                    icon = drawables.newLotIcon,
                    tint = colors.actionItemColors,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        profileNavigation.pushNew(
                            ProfileConfig.CreateOfferScreen(null, null, CreateOfferType.CREATE, null)
                        )
                    }
                ),
                NavigationItem(
                    title = getString(strings.myBidsTitle),
                    subtitle = getString(strings.myBidsSubTitle),
                    icon = drawables.bidsIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        try {
                            profileNavigation.replaceCurrent(
                                ProfileConfig.MyBidsScreen
                            )
                        } catch ( _ : Exception){}
                    }
                ),
                NavigationItem(
                    title = getString(strings.proposalTitle),
                    subtitle = getString(strings.proposalPriceSubTitle),
                    icon = drawables.proposalIcon,
                    tint = colors.black,
                    hasNews = false,
                    isVisible = true,
                    badgeCount = if((userInfo?.countUnreadPriceProposals ?:0) > 0)
                        userInfo?.countUnreadPriceProposals else null,
                    onClick = {
                        try {
                            profileNavigation.replaceCurrent(
                                ProfileConfig.MyProposalsScreen
                            )
                        } catch ( _ : Exception){}
                    }
                ),
                NavigationItem(
                    title = getString(strings.myPurchasesTitle),
                    subtitle = getString(strings.myPurchasesSubTitle),
                    icon = drawables.purchasesIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        try {
                            profileNavigation.replaceCurrent(
                                ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY)
                            )
                        } catch (_: Exception) {
                        }
                    }
                ),
                NavigationItem(
                    title = getString(strings.myOffersTitle),
                    subtitle = getString(strings.myOffersSubTitle),
                    icon = drawables.tagIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        try {
                            profileNavigation.replaceCurrent(
                                ProfileConfig.MyOffersScreen
                            )
                        } catch ( _ : Exception){}
                    }
                ),
                NavigationItem(
                    title = getString(strings.mySalesTitle),
                    subtitle = getString(strings.mySalesSubTitle),
                    icon = drawables.salesIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        try {
                            profileNavigation.replaceCurrent(
                                ProfileConfig.MyOrdersScreen(DealTypeGroup.SELL)
                            )
                        } catch ( _ : Exception){}
                    }
                ),
                NavigationItem(
                    title = getString(strings.messageTitle),
                    subtitle = getString(strings.messageSubTitle),
                    icon = drawables.dialogIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = if((userInfo?.countUnreadMessages ?:0) > 0) userInfo?.countUnreadMessages else null,
                    onClick = {
                        try {
                            profileNavigation.replaceCurrent(
                                ProfileConfig.ConversationsScreen()
                            )
                        } catch ( _ : Exception){
                        }
                    }
                ),
                NavigationItem(
                    title = getString(strings.myProfileTitle),
                    subtitle = getString(strings.myProfileSubTitle),
                    icon = drawables.profileIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        try {
                            profileNavigation.pushNew(
                                ProfileConfig.UserScreen(
                                    UserData.login,
                                    getCurrentDate(),
                                    false
                                )
                            )
                        } catch ( _ : Exception){}
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsProfileTitle),
                    subtitle = getString(strings.profileSettingsSubTitle),
                    icon = drawables.settingsIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        try {
                            profileNavigation.replaceCurrent(
                                ProfileConfig.ProfileSettingsScreen
                            )
                        } catch ( _ : Exception){}
                    }
                ),
                NavigationItem(
                    title = getString(strings.myBalanceTitle),
                    subtitle = getString(strings.myBalanceSubTitle),
                    icon = drawables.balanceIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null
                ),
                NavigationItem(
                    title = getString(strings.logoutTitle),
                    icon = drawables.logoutIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        setLogoutDialog(true)
                    }
                ),
            )
        }
    }

    fun debouncedNavigate(targetConfig : MainConfig) {
        try {
            val currentTime = (getCurrentDate().toLongOrNull() ?: 1L)*1000L
            if (currentTime - lastNavigationClickTime > NAVIGATION_DEBOUNCE_DELAY_MS) {
                lastNavigationClickTime = currentTime
                component.navigateToBottomItem(targetConfig)
            }
        }catch (_ : Exception){}
    }

    fun handleDeepLink(deepLink: DeepLink) {
        viewModelScope.launch {
            try {
                delay(300)
                withContext(Dispatchers.Main) {
                    when (deepLink) {
                        is DeepLink.GoToUser -> {
                            component.modelNavigation.value.homeNavigation.pushNew(
                                HomeConfig.UserScreen(
                                    deepLink.userId,
                                    getCurrentDate(),
                                    false
                                )
                            )
                        }

                        is DeepLink.GoToListing -> {
                            val categoryData = ListingData()
                            categoryData.searchData.userSearch = true
                            categoryData.searchData.userID = deepLink.ownerId
                            categoryData.searchData.userLogin = ""

                            component.modelNavigation.value.homeNavigation.pushNew(
                                HomeConfig.ListingScreen(
                                    false,
                                    categoryData.data,
                                    categoryData.searchData,
                                    getCurrentDate()
                                )
                            )
                        }

                        is DeepLink.GoToOffer -> {
                            component.modelNavigation.value.homeNavigation.pushNew(
                                HomeConfig.OfferScreen(
                                    deepLink.offerId,
                                    getCurrentDate()
                                )
                            )
                        }

                        is DeepLink.GoToAuth -> {
                            if (UserData.token == "")
                                goToLogin(true)
                        }

                        is DeepLink.GoToRegistration -> {
                            if (UserData.token == "")
                                goToLogin(true)
                        }

                        is DeepLink.GoToDynamicSettings -> {
                            goToDynamicSettings(deepLink.settingsType, deepLink.ownerId, deepLink.code)
                        }

                        is DeepLink.GoToVerification -> {
                            goToVerification(deepLink.settingsType ?: "", deepLink.ownerId, deepLink.code)
                        }

                        is DeepLink.GoToDialog -> {
                            if (deepLink.dialogId != 1L) {
                                when{
                                    component.childHomeStack.value.active.instance is ChildHome.MessagesChild -> {
                                        component.modelNavigation.value.homeNavigation.replaceCurrent(
                                            HomeConfig.MessagesScreen(
                                                deepLink.dialogId, deepLink.mes, getCurrentDate()
                                            )
                                        )
                                    }
                                    else -> {
                                        component.modelNavigation.value.homeNavigation.pushNew(
                                            HomeConfig.MessagesScreen(
                                                deepLink.dialogId, deepLink.mes, getCurrentDate()
                                            )
                                        )
                                    }
                                }
                            } else {
                                if (deepLink.mes != null) {
                                    component.navigateToBottomItem(
                                        MainConfig.Profile,
                                        "conversations/${deepLink.mes}"
                                    )
                                } else {
                                    component.navigateToBottomItem(MainConfig.Profile, "conversations")
                                }
                            }
                        }

                        is DeepLink.Unknown -> {}
                    }
                }
            }catch (_ : Exception){}
        }
    }

    fun updateOrientation(orientation: Int) {
        _showBottomBar.value = orientation == 0

        println("showBottomBar: ${showBottomBar.value}")
    }
}
