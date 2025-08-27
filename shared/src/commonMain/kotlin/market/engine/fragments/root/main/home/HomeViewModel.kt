package market.engine.fragments.root.main.home

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import market.engine.common.Platform
import market.engine.common.getPermissionHandler
import market.engine.common.openUrl
import market.engine.common.syncNotificationsFromUserDefaults
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
import market.engine.core.utils.CacheRepository
import market.engine.core.utils.deleteReadNotifications
import market.engine.core.utils.getMainTread
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.nowAsEpochSeconds
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.days


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

    private val cacheRepository = CacheRepository(db, mutex)

    val uiState: StateFlow<HomeUiState> = combine(
        updatePage,
        _responseCategory.state,
        _responseOffersPromotedOnMainPage1.state,
        _responseOffersPromotedOnMainPage2.state,
    )
    { up, categories, promoOffers1, promoOffers2 ->
        val userInfo = UserData.userInfo

        val proposalString = getString(strings.proposalTitle)
        val messageString = getString(strings.messageTitle)
        val notificationString = getString(strings.notificationTitle)

        val unreadCount = getUnreadNotificationsCount() ?: 0

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
                            getMainTread {
                                component.goToMyProposals()
                            }
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
                            getMainTread {
                                component.goToMessenger()
                            }
                        }
                    ),
                    NavigationItem(
                        title = notificationString,
                        isVisible = unreadCount > 0,
                        hasNews = false,
                        badgeCount = unreadCount,
                        icon = drawables.notification,
                        tint = colors.titleTextColor,
                        onClick = {
                            getMainTread {
                                component.goToNotificationHistory()
                            }
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
                        getMainTread {
                            component.goToContactUs()
                        }
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
                        getMainTread {
                            component.goToAppSettings()
                        }
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
        if(_responseOffersPromotedOnMainPage1.value.isEmpty()) {
            updateModel()
        }

        try {
            viewModelScope.launch {
                snapshotFlow { UserData.userInfo }
                    .collectLatest { newInfo ->
                        updatePage()
                    }
            }
        }catch (_ : Exception) {}

        analyticsHelper.reportEvent("view_main_page", mapOf())
    }

    fun updateModel() {
        refresh()

        updateUserInfo()

        getHistory(1L)

        syncNotificationsFromUserDefaults(db, mutex)

        viewModelScope.launch {
            deleteReadNotifications(db, mutex)
            updateCategoriesFromCacheOrNetwork()
            getOffersPromotedOnMainPage(0, 16)
            getOffersPromotedOnMainPage(1, 16)
        }
    }

    fun updateCategoriesFromCacheOrNetwork() {
        viewModelScope.launch {
            val cacheKey = "categories_home"
            val listSerializer = ListSerializer(Category.serializer())
            val lifetime = 30.days
            val expirationTimestamp = nowAsEpochSeconds() + lifetime.inWholeSeconds
            val cachedCategories = cacheRepository.get(cacheKey, listSerializer)

            if (cachedCategories == null) {
                getCategories(listingData = LD(), searchData = SD(), withoutCounter = true) {
                    viewModelScope.launch {
                        cacheRepository.put(cacheKey, it, expirationTimestamp, listSerializer)
                    }
                    _responseCategory.value = it
                }
            }else{
                _responseCategory.value = cachedCategories
            }
        }
    }

    fun getOffersPromotedOnMainPage(page: Int, ipp: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)

                val cacheKey = "home_page_${page}_${ipp}"
                val listSerializer = ListSerializer(OfferItem.serializer())
                val lifetime = 1.days
                val expirationTimestamp = nowAsEpochSeconds() + lifetime.inWholeSeconds
                val cachedCategories = cacheRepository.get(cacheKey, listSerializer)

                if (cachedCategories == null) {
                    val response = withContext(Dispatchers.IO) {
                        apiService.getOffersPromotedOnMainPage(page, ipp)
                    }
                    val serializer = Payload.serializer(Offer.serializer())
                    val payload: Payload<Offer> = deserializePayload(response.payload, serializer)
                    val newOffers = payload.objects.map { it.parseToOfferItem() }

                    viewModelScope.launch {
                        cacheRepository.put(cacheKey, newOffers, expirationTimestamp, listSerializer)
                    }

                    when(page){
                        0 ->{
                            _responseOffersPromotedOnMainPage1.value = newOffers
                        }
                        1 ->{
                            _responseOffersPromotedOnMainPage2.value = newOffers
                        }
                    }
                }else{
                    when(page) {
                        0 -> {
                            _responseOffersPromotedOnMainPage1.value = cachedCategories
                        }

                        1 -> {
                            _responseOffersPromotedOnMainPage2.value = cachedCategories
                        }
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
        getMainTread {
            val allPromo = getString(strings.allPromoOffersBtn)

            ld.data.filters.find { filter ->
                filter.key == "promo_main_page"
            }?.value = "promo_main_page"
            ld.data.filters.find { filter ->
                filter.key == "promo_main_page"
            }?.interpretation = allPromo

            ld.searchData.clear(allPromo)

            component.goToNewSearch(ld, false)
        }
    }

    fun goToCategory(category: TopCategory) {
        ld.searchData.searchCategoryID = category.id
        ld.searchData.searchParentID = category.parentId
        ld.searchData.searchCategoryName = category.name
        ld.searchData.searchParentName = category.parentName
        getMainTread {
            component.goToNewSearch(ld, false)
        }
    }

    suspend fun getUnreadNotificationsCount() : Int? {
        return mutex.withLock {
            try {
                val list = db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
                if (list.isEmpty()) null else list.filter { it.isRead == 0L }.size
            }catch (_ : Exception){
                null
            }
        }
    }
}
