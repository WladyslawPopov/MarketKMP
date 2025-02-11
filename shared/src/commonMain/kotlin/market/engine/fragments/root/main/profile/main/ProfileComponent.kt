package market.engine.fragments.root.main.profile.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.root.main.profile.navigation.MyOfferConfig
import market.engine.fragments.root.main.profile.navigation.ProfileConfig
import market.engine.fragments.root.main.profile.navigation.itemMyOffers
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.profile.myProposals.MyBidsComponent
import market.engine.fragments.root.main.profile.myOffers.MyOffersComponent
import market.engine.fragments.root.main.profile.myOrders.MyOrdersComponent
import market.engine.fragments.root.main.profile.navigation.MyBidsConfig
import market.engine.fragments.root.main.profile.navigation.MyOrderConfig
import market.engine.fragments.root.main.profile.navigation.ProfileSettingsConfig
import market.engine.fragments.root.main.profile.navigation.itemMyBids
import market.engine.fragments.root.main.profile.navigation.itemMyOrders
import market.engine.fragments.root.main.profile.navigation.itemProfileSettings
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsComponent
import org.koin.mp.KoinPlatform.getKoin

interface ProfileComponent {
    val model : Value<Model>

    val myOffersPages: Value<ChildPages<*, MyOffersComponent>>

    val myOrdersPages: Value<ChildPages<*, MyOrdersComponent>>

    val myBidsPages: Value<ChildPages<*, MyBidsComponent>>

    val settingsPages: Value<ChildPages<*, ProfileSettingsComponent>>

    data class Model(
        val navigationItems: List<NavigationItem>,
        val profileViewModel: ProfileViewModel,
        val backHandler: BackHandler
    )

    fun updateProfile()
    fun goToAllMyOfferListing()
    fun goToAboutMe()
    fun selectProfileSettingsPage(type: ProfileSettingsTypes)
    fun selectMyOfferPage(type: LotsType)
    fun selectMyBidsPage(type: LotsType)
    fun selectMyOrderPage(type: DealType)
    fun goToSubscribe()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    selectedPage : String?,
    val navigationItems: List<NavigationItem>,
    private val navigationProfile: StackNavigation<ProfileConfig>,
    private val navigateToDynamicSettings : (String) -> Unit,
    private val navigateToSubscriptions : () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    private val navigationMyOffers = PagesNavigation<MyOfferConfig>()
    private val navigationMyBids = PagesNavigation<MyBidsConfig>()
    private val navigationMyOrders = PagesNavigation<MyOrderConfig>()
    private val navigationSettings = PagesNavigation<ProfileSettingsConfig>()


    private val _model = MutableValue(
        ProfileComponent.Model(
            navigationItems = navigationItems,
            profileViewModel = getKoin().get(),
            backHandler = backHandler
        )
    )
    override val model = _model


    private var currentPage = ""
    private var searchID : Long? = null


    init {
        lifecycle.doOnResume {
            updateProfile()

            searchID = selectedPage?.split("/")?.lastOrNull()?.toLongOrNull()
            currentPage = selectedPage?.split("/")?.firstOrNull() ?: ""

            when (currentPage) {
                "conversations" -> {
                    navigationProfile.replaceAll(ProfileConfig.ConversationsScreen)
                }
                "purchases" -> {
                    navigationProfile.replaceAll(ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY, searchID))
                }
                "sales" -> {
                    navigationProfile.replaceAll(ProfileConfig.MyOrdersScreen(DealTypeGroup.SELL, searchID))
                }
            }

            model.value.profileViewModel.analyticsHelper.reportEvent(
                "view_profile",
                mapOf("user_id" to UserData.login)
            )
        }
    }

    override fun updateProfile() {
        model.value.profileViewModel.getUserInfo(UserData.login)
    }

    override fun goToAllMyOfferListing() {
        val ld = ListingData()
        val searchData = ld.searchData
        searchData.value.userID = UserData.login
        searchData.value.userLogin = UserData.userInfo?.login
        searchData.value.userSearch = true
        navigationProfile.pushNew(
            ProfileConfig.ListingScreen(ld.data.value, ld.searchData.value)
        )
    }

    override fun goToAboutMe() {
        navigationProfile.pushNew(
            ProfileConfig.UserScreen(UserData.login, getCurrentDate(), true)
        )
    }

    override fun selectMyOrderPage(type: DealType) {
        when (type){
            DealType.SELL_ALL -> {
                navigationMyOrders.select(0)
            }
            DealType.SELL_ARCHIVE -> {
                navigationMyOrders.select(2)
            }
            DealType.SELL_IN_WORK -> {
                navigationMyOrders.select(1)
            }
            DealType.BUY_ARCHIVE -> {
                navigationMyOrders.select(1)
            }
            DealType.BUY_IN_WORK -> {
                navigationMyOrders.select(0)
            }
        }
    }

    override fun goToSubscribe() {
        navigateToSubscriptions()
    }

    override fun selectMyOfferPage(type: LotsType) {
        when (type) {
            LotsType.MYLOT_ACTIVE -> {
                navigationMyOffers.select(0)
            }

            LotsType.MYLOT_UNACTIVE -> {
                navigationMyOffers.select(1)
            }

            LotsType.MYLOT_FUTURE -> {
                navigationMyOffers.select(2)
            }
            else -> {
                navigationMyOffers.select(0)
            }
        }
    }

    override fun selectMyBidsPage(type: LotsType) {
        when (type) {
            LotsType.MYBIDLOTS_ACTIVE -> {
                navigationMyBids.select(0)
            }

            LotsType.MYBIDLOTS_UNACTIVE -> {
                navigationMyBids.select(1)
            }

            else -> {
                navigationMyBids.select(0)
            }
        }
    }

    override fun selectProfileSettingsPage(type: ProfileSettingsTypes) {
        when (type) {
            ProfileSettingsTypes.GLOBAL_SETTINGS -> {
                navigationSettings.select(0)
            }

            ProfileSettingsTypes.SELLER_SETTINGS -> {
                navigationSettings.select(1)
            }

            ProfileSettingsTypes.ADDITIONAL_SETTINGS -> {
                navigationSettings.select(2)
            }
        }
    }

    override val settingsPages: Value<ChildPages<*, ProfileSettingsComponent>> by lazy {
        childPages(
            source = navigationSettings,
            serializer = ProfileSettingsConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    listOf(
                        ProfileSettingsConfig(ProfileSettingsTypes.GLOBAL_SETTINGS),
//                        ProfileSettingsConfig(ProfileSettingsTypes.SELLER_SETTINGS),
//                        ProfileSettingsConfig(ProfileSettingsTypes.ADDITIONAL_SETTINGS)
                    ),
                    selectedIndex = 0,
                )
            },
            key = "ProfileSettingsStack",
            childFactory = { config, componentContext ->
                itemProfileSettings(
                    config,
                    componentContext,
                    navigationProfile,
                    selectProfileSettingsPage = { type ->
                        selectProfileSettingsPage(type)
                    },
                    selectDynamicSettings = navigateToDynamicSettings
                )
            }
        )
    }

    override val myOffersPages: Value<ChildPages<*, MyOffersComponent>> by lazy {
        childPages(
            source = navigationMyOffers,
            serializer = MyOfferConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    listOf(
                        MyOfferConfig(lotsType = LotsType.MYLOT_ACTIVE),
                        MyOfferConfig(lotsType = LotsType.MYLOT_UNACTIVE),
                        MyOfferConfig(lotsType = LotsType.MYLOT_FUTURE)
                    ),
                    selectedIndex = 0,
                )
            },
            key = "ProfileMyOffersStack",
            childFactory = { config, componentContext ->
                itemMyOffers(config, componentContext, navigationProfile, selectMyOfferPage = { type ->
                    selectMyOfferPage(type)
                })
            }
        )
    }

    override val myBidsPages: Value<ChildPages<*, MyBidsComponent>> by lazy {
        childPages(
            source = navigationMyBids,
            serializer = MyBidsConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    listOf(
                        MyBidsConfig(lotsType = LotsType.MYBIDLOTS_ACTIVE),
                        MyBidsConfig(lotsType = LotsType.MYBIDLOTS_UNACTIVE),
                    ),
                    selectedIndex = 0,
                )
            },
            key = "ProfileMyBidsStack",
            childFactory = { config, componentContext ->
                itemMyBids(config, componentContext, navigationProfile, ::selectMyBidsPage)
            }
        )
    }

    override val myOrdersPages: Value<ChildPages<*, MyOrdersComponent>> by lazy {
        childPages(
            source = navigationMyOrders,
            serializer = MyOrderConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    when (currentPage){
                        "purchases" -> {
                            listOf(
                                MyOrderConfig(
                                    id = searchID,
                                    dealType = DealType.BUY_IN_WORK
                                ),
                                MyOrderConfig(
                                    dealType = DealType.BUY_ARCHIVE
                                )
                            )
                        }
                        "sales" -> {
                            listOf(
                                MyOrderConfig(
                                    id = searchID,
                                    dealType = DealType.SELL_ALL
                                ),
                                MyOrderConfig(
                                    dealType = DealType.SELL_IN_WORK
                                ),
                                MyOrderConfig(
                                    dealType = DealType.SELL_ARCHIVE
                                )
                            )
                        }
                        else -> {
                            listOf()
                        }
                    },
                    selectedIndex = 0,
                )
            },
            key = "ProfileMyOrdersStack",
            childFactory = { config, componentContext ->
                itemMyOrders(
                    config,
                    componentContext,
                    navigationProfile,
                    selectMyOrderPage = { type ->
                        selectMyOrderPage(type)
                    }
                )
            }
        )
    }
}
