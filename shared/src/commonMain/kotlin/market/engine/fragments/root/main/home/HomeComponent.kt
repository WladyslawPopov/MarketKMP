package market.engine.fragments.root.main.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.launch
import market.engine.common.Platform
import market.engine.common.getPermissionHandler
import market.engine.common.openUrl
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.TopCategory
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deleteReadNotifications
import org.jetbrains.compose.resources.getString

interface HomeEvents {
    fun onRefresh()
    fun goToLogin()
    fun goToOffer(id: Long)
    fun goToNewSearch()
    fun goToCategory(category: TopCategory)
    fun goToAllPromo()
    fun goToCreateOffer()
    fun goToMessenger()
    fun goToContactUs()
    fun goToAppSettings()
    fun goToMyProposals()
    fun goToNotificationHistory()
}

interface HomeComponent {
    val model: Value<Model>
    data class Model(
        val listingData: ListingData,
        val homeViewModel: HomeViewModel,
        val backHandler: BackHandler,
        val events: HomeEvents
    )
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    val navigation: StackNavigation<HomeConfig>,
    private val navigateToListingSelected: (ListingData, Boolean) -> Unit,
    val navigateToLoginSelected: () -> Unit,
    val navigateToOfferSelected: (id: Long) -> Unit,
    val navigateToCreateOfferSelected: () -> Unit,
    val navigateToMessengerSelected: () -> Unit,
    val navigateToContactUsSelected: () -> Unit,
    val navigateToSettingsSelected: () -> Unit,
    val navigateToMyProposalsSelected: () -> Unit,
    val navigateToNotificationHistorySelected: () -> Unit,
) : HomeComponent, ComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = HomeViewModel()

    private val analyticsHelper = homeViewModel.analyticsHelper

    private val userRepository = homeViewModel.userRepository

    private val listingData = ListingData()

    private val _model = MutableValue(
        HomeComponent.Model(
            listingData,
            homeViewModel,
            backHandler,
            events = object : HomeEvents {
                    override fun onRefresh() {
                        updateModel()
                    }

                    override fun goToLogin() {
                        navigateToLoginSelected()
                    }

                    override fun goToOffer(id: Long) {
                        navigateToOfferSelected(id)
                    }

                    override fun goToNewSearch() {
                        navigateToListingSelected(listingData, true)
                    }

                    override fun goToCategory(category: TopCategory) {
                        listingData.searchData.value.searchCategoryID = category.id
                        listingData.searchData.value.searchParentID = category.parentId
                        listingData.searchData.value.searchCategoryName = category.name
                        listingData.searchData.value.searchParentName = category.parentName

                        navigateToListingSelected(listingData, false)
                    }

                    override fun goToAllPromo() {
                        listingData.data.value.filters = ListingFilters.getEmpty()
                        model.value.homeViewModel.viewModelScope.launch {
                            val allPromo = getString(strings.allPromoOffersBtn)

                            listingData.data.value.filters.find {
                                    filter -> filter.key == "promo_main_page"
                            }?.value = "promo_main_page"
                            listingData.data.value.filters.find {
                                    filter -> filter.key == "promo_main_page"
                            }?.interpretation = allPromo

                            listingData.searchData.value.clear(allPromo)

                            navigateToListingSelected(listingData, false)
                        }
                    }

                    override fun goToCreateOffer() {
                        navigateToCreateOfferSelected()
                    }

                    override fun goToMessenger() {
                        navigateToMessengerSelected()
                    }

                    override fun goToContactUs() {
                        navigateToContactUsSelected()
                    }

                    override fun goToAppSettings() {
                        navigateToSettingsSelected()
                    }

                    override fun goToMyProposals() {
                        navigateToMyProposalsSelected()
                    }

                    override fun goToNotificationHistory() {
                        navigateToNotificationHistorySelected()
                    }
                }
        )
    )

    override val model: Value<HomeComponent.Model> = _model

    init {
        getPermissionHandler().askPermissionNotification()
        userRepository.updateToken()
        updateModel()
        analyticsHelper.reportEvent("view_main_page", mapOf())

        lifecycle.doOnResume {
            if (homeViewModel.uiState.value.promoOffers1.isEmpty()){
                updateModel()
            }

            deleteReadNotifications()
        }
    }

    private fun updateModel() {
        homeViewModel.onError(ServerErrorException())

        homeViewModel.updateUserInfo()
        homeViewModel.getCategories(listingData = LD(), searchData = SD(), withoutCounter =  true){
            homeViewModel.setCategory(it)
        }
        homeViewModel.getOffersPromotedOnMainPage(0, 16)
        homeViewModel.getOffersPromotedOnMainPage(1, 16)
        getAppBarList()
    }

    fun getAppBarList() {
        homeViewModel.viewModelScope.launch {
            val userInfo = UserData.userInfo
            homeViewModel.listAppBar.value = listOf(
                NavigationItem(
                    title = "",
                    icon = drawables.recycleIcon,
                    tint = colors.inactiveBottomNavIconColor,
                    hasNews = false,
                    isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                    badgeCount = null,
                    onClick = { model.value.events.onRefresh() }
                ),
                NavigationItem(
                    title = getString(strings.proposalTitle),
                    icon = drawables.currencyIcon,
                    tint = colors.titleTextColor,
                    hasNews = false,
                    badgeCount = userInfo?.countUnreadPriceProposals,
                    isVisible = (userInfo?.countUnreadPriceProposals ?: 0) > 0,
                    onClick = {
                        model.value.events.goToMyProposals()
                    }
                ),
                NavigationItem(
                    title = getString(strings.messageTitle),
                    icon = drawables.mail,
                    tint = colors.brightBlue,
                    hasNews = false,
                    badgeCount = if ((userInfo?.countUnreadMessages
                            ?: 0) > 0
                    ) (userInfo?.countUnreadMessages ?: 0) else null,
                    onClick = {
                        model.value.events.goToMessenger()
                    }
                ),
                NavigationItem(
                    title = getString(strings.notificationTitle),
                    icon = drawables.notification,
                    tint = colors.titleTextColor,
                    isVisible = (homeViewModel.getUnreadNotificationsCount() ?: 0) > 0,
                    hasNews = false,
                    badgeCount = homeViewModel.getUnreadNotificationsCount(),
                    onClick = {
                        navigateToNotificationHistorySelected()
                    }
                ),
            )
            homeViewModel.drawerList.value = listOf(
                NavigationItem(
                    title = getString(strings.top100Title),
                    subtitle = getString(strings.top100Subtitle),
                    icon = drawables.top100Icon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        openUrl("${SAPI.SERVER_BASE}rating_game")
                    }
                ),
                NavigationItem(
                    title = getString(strings.helpTitle),
                    subtitle = getString(strings.helpSubtitle),
                    icon = drawables.helpIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        openUrl("${SAPI.SERVER_BASE}help/general")
                    }
                ),
                NavigationItem(
                    title = getString(strings.contactUsTitle),
                    subtitle = getString(strings.contactUsSubtitle),
                    icon = drawables.contactUsIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        model.value.events.goToContactUs()
                    }
                ),
                NavigationItem(
                    title = getString(strings.aboutUsTitle),
                    subtitle = getString(strings.aboutUsSubtitle),
                    icon = drawables.infoIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        openUrl("${SAPI.SERVER_BASE}staticpage/doc/about_us")
                    }
                ),
                NavigationItem(
                    title = getString(strings.reviewsTitle),
                    subtitle = getString(strings.reviewsSubtitle),
                    icon = drawables.starIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    isVisible = SAPI.REVIEW_URL != "",
                    onClick = {
                        openUrl(SAPI.REVIEW_URL)
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsTitleApp),
                    subtitle = getString(strings.settingsSubtitleApp),
                    icon = drawables.settingsIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        model.value.events.goToAppSettings()
                    }
                ),
            )
            homeViewModel.listFooter.value = listOf(
                TopCategory(
                    id = 1,
                    name = getString(strings.homeFixAuction),
                    icon = drawables.auctionFixIcon
                ),
                TopCategory(
                    id = 2,
                    name = getString(strings.homeManyOffers),
                    icon = drawables.manyOffersIcon
                ),
                TopCategory(
                    id = 3,
                    name = getString(strings.verifySellers),
                    icon = drawables.verifySellersIcon
                ),
                TopCategory(
                    id = 4,
                    name = getString(strings.everyDeyDiscount),
                    icon = drawables.discountBigIcon
                ),
                TopCategory(
                    id = 5,
                    name = getString(strings.freeBilling),
                    icon = drawables.freeBillingIcon
                ),
            )
        }
    }
}
