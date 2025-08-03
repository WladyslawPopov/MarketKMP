package market.engine.fragments.root.main.home

import androidx.lifecycle.SavedStateHandle
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import market.engine.common.Platform
import market.engine.common.getPermissionHandler
import market.engine.common.openUrl
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.TopCategory
import market.engine.core.data.states.HomeUiState
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.Category
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.printLogD
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString


class HomeViewModel(val component: HomeComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {

    companion object {
        private const val PROMO_1_KEY = "PromotedOnMainPage1Json"
        private const val PROMO_2_KEY = "PromotedOnMainPage2Json"
        private const val CATEGORIES_KEY = "CategoriesJson"
    }

    private val _responseOffersPromotedOnMainPage1 = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = PROMO_1_KEY,
        initialValue = emptyList(),
        serializer = ListSerializer(OfferItem.serializer())
    )

    private val _responseOffersPromotedOnMainPage2 = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = PROMO_2_KEY,
        initialValue = emptyList(),
        serializer = ListSerializer(OfferItem.serializer())
    )

    private val _responseCategory = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = CATEGORIES_KEY,
        initialValue = emptyList(),
        serializer = ListSerializer(Category.serializer())
    )

    private val ld = ListingData()

    val uiState: StateFlow<HomeUiState> = combine(
        updatePage,
        _responseCategory.state,
        _responseOffersPromotedOnMainPage1.state,
        _responseOffersPromotedOnMainPage2.state,
    ) { up, categories, promoOffers1, promoOffers2 ->
        val userInfo = UserData.userInfo

        val proposalString = getString(strings.proposalTitle)
        val messageString = getString(strings.messageTitle)
        val notificationString = getString(strings.notificationTitle)

        HomeUiState(
            categories = categories.map {
                TopCategory(
                    id = it.id,
                    parentId = it.parentId,
                    name = it.name ?: getString(strings.categoryMain),
                    parentName = null,
                    icon = drawables.infoIcon
                )
            },
            promoOffers1 = promoOffers1,
            promoOffers2 = promoOffers2,
            unreadNotificationsCount = getUnreadNotificationsCount(),
            appBarData = SimpleAppBarData(
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        onClick = { updateModel() }
                    ),
                    NavigationItem(
                        title = proposalString,
                        hasNews = false,
                        badgeCount = userInfo?.countUnreadPriceProposals,
                        isVisible = (userInfo?.countUnreadPriceProposals ?: 0) > 0,
                        icon = drawables.currencyIcon,
                        tint = colors.titleTextColor,
                        onClick = {
                            component.goToMyProposals()
                        }
                    ),
                    NavigationItem(
                        title = messageString,
                        hasNews = false,
                        badgeCount = if ((userInfo?.countUnreadMessages
                                ?: 0) > 0
                        ) (userInfo?.countUnreadMessages ?: 0) else null,
                        icon = drawables.mail,
                        tint = colors.brightBlue,
                        onClick = {
                            component.goToMessenger()
                        }
                    ),
                    NavigationItem(
                        title = notificationString,
                        isVisible = (getUnreadNotificationsCount() ?: 0) > 0,
                        hasNews = false,
                        badgeCount = getUnreadNotificationsCount(),
                        icon = drawables.notification,
                        tint = colors.titleTextColor,
                        onClick = {
                            component.goToNotificationHistory()
                        }
                    ),
                )
            ),
            listFooter = listOf(
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
                )
            ),
            drawerList = listOf(
                NavigationItem(
                    title = getString(strings.top100Title),
                    hasNews = false,
                    badgeCount = null,
                    icon = drawables.top100Icon,
                    tint = colors.black,
                    onClick = {
                        openUrl("${SAPI.SERVER_BASE}rating_game")
                    }
                ),
                NavigationItem(
                    title = getString(strings.helpTitle),
                    subtitle = getString(strings.helpSubtitle),
                    hasNews = false,
                    badgeCount = null,
                    icon = drawables.helpIcon,
                    tint = colors.black,
                    onClick = {
                        openUrl("${SAPI.SERVER_BASE}help/general")
                    }
                ),
                NavigationItem(
                    title = getString(strings.contactUsTitle),
                    subtitle = getString(strings.contactUsSubtitle),
                    hasNews = false,
                    badgeCount = null,
                    icon = drawables.contactUsIcon,
                    tint = colors.black,
                    onClick = {
                        component.goToContactUs()
                    }
                ),
                NavigationItem(
                    title = getString(strings.aboutUsTitle),
                    subtitle = getString(strings.aboutUsSubtitle),
                    hasNews = false,
                    badgeCount = null,
                    icon = drawables.infoIcon,
                    tint = colors.black,
                    onClick = {
                        openUrl("${SAPI.SERVER_BASE}staticpage/doc/about_us")
                    }
                ),
                NavigationItem(
                    title = getString(strings.reviewsTitle),
                    subtitle = getString(strings.reviewsSubtitle),
                    hasNews = false,
                    badgeCount = null,
                    isVisible = SAPI.REVIEW_URL != "",
                    icon = drawables.starIcon,
                    tint = colors.black,
                    onClick = {
                        openUrl(SAPI.REVIEW_URL)
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsTitleApp),
                    subtitle = getString(strings.settingsSubtitleApp),
                    hasNews = false,
                    badgeCount = null,
                    icon = drawables.settingsIcon,
                    tint = colors.black,
                    onClick = {
                        component.goToAppSettings()
                    }
                )
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState()
    )

    init {
        getPermissionHandler().askPermissionNotification()
        userRepository.updateToken()
        printLogD("start viewModel", _responseOffersPromotedOnMainPage1.value.toString())
        if(_responseOffersPromotedOnMainPage1.value.isEmpty()) {
            updateModel()
        }
        analyticsHelper.reportEvent("view_main_page", mapOf())
    }

    fun updateModel() {
        refresh()

        updateUserInfo()
        getCategories(listingData = LD(), searchData = SD(), withoutCounter = true) {
            setCategory(it)
        }
        getOffersPromotedOnMainPage(0, 16)
        getOffersPromotedOnMainPage(1, 16)
    }

    fun setCategory(category: List<Category>) {
        viewModelScope.launch {
            _responseCategory.value = category
        }
    }

    fun getOffersPromotedOnMainPage(page: Int, ipp: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) {
                    apiService.getOffersPromotedOnMainPage(page, ipp)
                }
                val serializer = Payload.serializer(Offer.serializer())
                val payload: Payload<Offer> = deserializePayload(response.payload, serializer)
                when(page){
                    0 -> {
                        val newOffers = payload.objects.map { it.parseToOfferItem() }
                        _responseOffersPromotedOnMainPage1.value = newOffers
                    }
                    1 ->{
                        _responseOffersPromotedOnMainPage2.value = payload.objects.map { it.parseToOfferItem() }
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", "An error occurred"))
            } finally {
                setLoading(false)
            }
        }
    }

    fun goToAllPromo() {
        ld.data.filters = ListingFilters.getEmpty()
        viewModelScope.launch {
            val allPromo = getString(strings.allPromoOffersBtn)

            ld.data.filters.find { filter ->
                filter.key == "promo_main_page"
            }?.value = "promo_main_page"
            ld.data.filters.find { filter ->
                filter.key == "promo_main_page"
            }?.interpretation = allPromo

            ld.searchData.clear(allPromo)
            withContext(Dispatchers.Main) {
                component.goToNewSearch(ld, false)
            }
        }
    }

    fun goToCategory(category: TopCategory) {
        ld.searchData.searchCategoryID = category.id
        ld.searchData.searchParentID = category.parentId
        ld.searchData.searchCategoryName = category.name
        ld.searchData.searchParentName = category.parentName

        component.goToNewSearch(ld, false)
    }

    fun getUnreadNotificationsCount() : Int? {
        return try {
            val list = db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
            if (list.isEmpty()) null else list.filter { it.isRead < 1 || it.isRead > 1 }.size
        }catch (_ : Exception){
            null
        }
    }
}
