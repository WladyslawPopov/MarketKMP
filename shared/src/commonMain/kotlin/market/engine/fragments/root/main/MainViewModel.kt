package market.engine.fragments.root.main

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import market.engine.common.Platform
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.DefaultRootComponent.Companion.goToVerification
import market.engine.fragments.root.main.home.ChildHome
import market.engine.fragments.root.main.home.HomeConfig
import org.jetbrains.compose.resources.getString

class MainViewModel(val component: MainComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {
    private val _showBottomBar = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "showBottomBar",
        checkShowBar(),
        Boolean.serializer()
    )
    val showBottomBar = _showBottomBar.state

    private val _bottomList = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "bottomList",
        emptyList(),
        ListSerializer(NavigationItem.serializer())
    )
    val bottomList = _bottomList.state

    private val _publicProfileNavigationItems = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "publicProfileNavigationItems",
        emptyList(),
        ListSerializer(NavigationItem.serializer())
    )
    val publicProfileNavigationItems = _publicProfileNavigationItems.state

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

    suspend fun updateNavLists() {
        val userInfo = UserData.userInfo
        withContext(Dispatchers.Main) {
            _bottomList.value = listOf(
                NavigationItem(
                    title = getString(strings.homeTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.searchTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.basketTitle),
                    hasNews = false,
                    badgeCount = if ((userInfo?.countOffersInCart
                            ?: 0) > 0
                    ) userInfo?.countOffersInCart else null,
                ),
                NavigationItem(
                    title = getString(strings.favoritesTitle),
                    hasNews = false,
                    badgeCount = if ((userInfo?.countWatchedOffers
                            ?: 0) > 0
                    ) userInfo?.countWatchedOffers else null,
                ),
                NavigationItem(
                    title = getString(strings.profileTitleBottom),
                    imageString = userInfo?.avatar?.thumb?.content,
                    hasNews = (
                            (userInfo?.countUnreadMessages ?: 0) > 0 ||
                                    (userInfo?.countUnreadPriceProposals ?: 0) > 0
                            ),
                    badgeCount = null
                )
            )

            _publicProfileNavigationItems.value = listOf(
                NavigationItem(
                    title = getString(strings.createNewOfferTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.myBidsTitle),
                    subtitle = getString(strings.myBidsSubTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.proposalTitle),
                    subtitle = getString(strings.proposalPriceSubTitle),
                    hasNews = false,
                    isVisible = true,
                    badgeCount = if ((userInfo?.countUnreadPriceProposals ?: 0) > 0)
                        userInfo?.countUnreadPriceProposals else null,
                ),
                NavigationItem(
                    title = getString(strings.myPurchasesTitle),
                    subtitle = getString(strings.myPurchasesSubTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.myOffersTitle),
                    subtitle = getString(strings.myOffersSubTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.mySalesTitle),
                    subtitle = getString(strings.mySalesSubTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.messageTitle),
                    subtitle = getString(strings.messageSubTitle),
                    hasNews = false,
                    badgeCount = if ((userInfo?.countUnreadMessages
                            ?: 0) > 0
                    ) userInfo?.countUnreadMessages else null,
                ),
                NavigationItem(
                    title = getString(strings.myProfileTitle),
                    subtitle = getString(strings.myProfileSubTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.settingsProfileTitle),
                    subtitle = getString(strings.profileSettingsSubTitle),
                    hasNews = false,
                    badgeCount = null,
                ),
                NavigationItem(
                    title = getString(strings.myBalanceTitle),
                    subtitle = getString(strings.myBalanceSubTitle),
                    hasNews = false,
                    badgeCount = null
                ),
                NavigationItem(
                    title = getString(strings.logoutTitle),
                    hasNews = false,
                    badgeCount = null,
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
                when{
                    deepLink.ownerId != null -> {
                        categoryData.searchData.userID = deepLink.ownerId
                        categoryData.searchData.userLogin = ""
                    }
                    deepLink.categoryId != null -> {
                        categoryData.searchData.searchCategoryID = deepLink.categoryId
                        categoryData.searchData.searchCategoryName = deepLink.categoryName ?: ""
                    }
                    else -> {

                    }
                }

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

    fun updateOrientation(orientation: Int) {
        _showBottomBar.value = orientation == 0

        println("showBottomBar: ${showBottomBar.value}")
    }
}
