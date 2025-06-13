package market.engine.fragments.root.main.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.Platform
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
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.Category
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.bars.DrawerAppBarData
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

interface HomeEvents {
    fun goToCategory(category: TopCategory)
    fun goToAllPromo()
}

data class HomeUiState(
    val categories: List<TopCategory> = emptyList(),
    val promoOffers1: List<OfferItem> = emptyList(),
    val promoOffers2: List<OfferItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: ServerErrorException = ServerErrorException(),
    val unreadNotificationsCount: Int? = null,
    val appBarData : DrawerAppBarData? = null,
    val events : HomeEvents? = null,
    val listFooter: List<TopCategory> = emptyList(),
    val drawerList: List<NavigationItem> = emptyList(),
)

class HomeViewModel(component: HomeComponent) : BaseViewModel() {

    private val _responseOffersPromotedOnMainPage1 = MutableStateFlow<List<OfferItem>>(emptyList())
    private val _responseOffersPromotedOnMainPage2 = MutableStateFlow<List<OfferItem>>(emptyList())
    private val _responseCategory = MutableStateFlow<List<TopCategory>>(emptyList())

    private val listingData = ListingData()


    val uiState: StateFlow<HomeUiState> = combine(
        _responseCategory,
        _responseOffersPromotedOnMainPage1,
        _responseOffersPromotedOnMainPage2,
        isShowProgress,
        errorMessage
    ) { categories, promoOffers1, promoOffers2, isLoading, error ->
        val userInfo = UserData.userInfo

        val proposalString = getString(strings.proposalTitle)
        val messageString = getString(strings.messageTitle)
        val notificationString = getString(strings.notificationTitle)

        HomeUiState(
            categories = categories,
            promoOffers1 = promoOffers1,
            promoOffers2 = promoOffers2,
            isLoading = isLoading,
            error = error,
            unreadNotificationsCount = getUnreadNotificationsCount(),
            appBarData = object : DrawerAppBarData {
                override val modifier: Modifier
                    get() = Modifier.fillMaxWidth()
                override val color: Color
                    get() = colors.white
                override val content: @Composable (() -> Unit)
                    get() = {
                        Image(
                            painter = painterResource(drawables.logo),
                            contentDescription = stringResource(strings.homeTitle),
                            modifier = Modifier.size(140.dp, 68.dp),
                        )
                    }
                override val listItems: List<NavigationItem>
                    get() = listOf(
                        NavigationItem(
                            title = "",
                            icon = drawables.recycleIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            hasNews = false,
                            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                            badgeCount = null,
                            onClick = { updateModel() }
                        ),
                        NavigationItem(
                            title = proposalString,
                            icon = drawables.currencyIcon,
                            tint = colors.titleTextColor,
                            hasNews = false,
                            badgeCount = userInfo?.countUnreadPriceProposals,
                            isVisible = (userInfo?.countUnreadPriceProposals ?: 0) > 0,
                            onClick = {
                                component.goToMyProposals()
                            }
                        ),
                        NavigationItem(
                            title = messageString,
                            icon = drawables.mail,
                            tint = colors.brightBlue,
                            hasNews = false,
                            badgeCount = if ((userInfo?.countUnreadMessages
                                    ?: 0) > 0
                            ) (userInfo?.countUnreadMessages ?: 0) else null,
                            onClick = {
                                component.goToMessenger()
                            }
                        ),
                        NavigationItem(
                            title = notificationString,
                            icon = drawables.notification,
                            tint = colors.titleTextColor,
                            isVisible = (getUnreadNotificationsCount() ?: 0) > 0,
                            hasNews = false,
                            badgeCount = getUnreadNotificationsCount(),
                            onClick = {
                                component.goToNotificationHistory()
                            }
                        ),
                    )
            },
            events = object : HomeEvents {
                override fun goToCategory(category: TopCategory) {
                    listingData.searchData.searchCategoryID = category.id
                    listingData.searchData.searchParentID = category.parentId
                    listingData.searchData.searchCategoryName = category.name
                    listingData.searchData.searchParentName = category.parentName

                    component.goToNewSearch(listingData, false)
                }
                override fun goToAllPromo() {
                    listingData.data.filters = ListingFilters.getEmpty()
                    viewModelScope.launch {
                        val allPromo = getString(strings.allPromoOffersBtn)

                        listingData.data.filters.find {
                                filter -> filter.key == "promo_main_page"
                        }?.value = "promo_main_page"
                        listingData.data.filters.find {
                                filter -> filter.key == "promo_main_page"
                        }?.interpretation = allPromo

                        listingData.searchData.clear(allPromo)

                        component.goToNewSearch(listingData, false)
                    }
                }
            },
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
                        component.goToContactUs()
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
                        component.goToAppSettings()
                    }
                ),
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState()
    )

    fun updateModel() {
        onError(ServerErrorException())

        updateUserInfo()
        getCategories(listingData = LD(), searchData = SD(), withoutCounter = true) {
            setCategory(it)
        }
        getOffersPromotedOnMainPage(0, 16)
        getOffersPromotedOnMainPage(1, 16)

    }
    fun setCategory(category: List<Category>) {
        _responseCategory.value = category.map {
            TopCategory(
                id = it.id,
                parentId = it.parentId,
                name = it.name ?: catDef.value,
                parentName = null,
                icon = drawables.infoIcon
            )
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
                    0 -> _responseOffersPromotedOnMainPage1.value = payload.objects.map { it.parseToOfferItem() }
                    1 -> _responseOffersPromotedOnMainPage2.value = payload.objects.map { it.parseToOfferItem() }
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
}
