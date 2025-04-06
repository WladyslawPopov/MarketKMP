package market.engine.fragments.root.main.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.root.main.profile.navigation.MyOfferConfig
import market.engine.fragments.root.main.profile.navigation.ProfileConfig
import market.engine.fragments.root.main.profile.navigation.itemMyOffers
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.main.profile.myBids.MyBidsComponent
import market.engine.fragments.root.main.profile.myOffers.MyOffersComponent
import market.engine.fragments.root.main.profile.myOrders.MyOrdersComponent
import market.engine.fragments.root.main.profile.myProposals.MyProposalsComponent
import market.engine.fragments.root.main.profile.navigation.MyBidsConfig
import market.engine.fragments.root.main.profile.navigation.MyOrderConfig
import market.engine.fragments.root.main.profile.navigation.MyProposalsConfig
import market.engine.fragments.root.main.profile.navigation.ProfileSettingsConfig
import market.engine.fragments.root.main.profile.navigation.itemMyBids
import market.engine.fragments.root.main.profile.navigation.itemMyOrders
import market.engine.fragments.root.main.profile.navigation.itemMyProposals
import market.engine.fragments.root.main.profile.navigation.itemProfileSettings
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsComponent

interface ProfileChildrenComponent {
    val model : Value<Model>

    val myOffersPages: Value<ChildPages<*, MyOffersComponent>>

    val myOrdersPages: Value<ChildPages<*, MyOrdersComponent>>

    val myBidsPages: Value<ChildPages<*, MyBidsComponent>>

    val myProposalsPages: Value<ChildPages<*, MyProposalsComponent>>

    val settingsPages: Value<ChildPages<*, ProfileSettingsComponent>>

    data class Model(
        val backHandler: BackHandler
    )

    fun selectProfileSettingsPage(type: ProfileSettingsTypes)
    fun selectOfferPage(type: LotsType)
    fun selectMyOrderPage(type: DealType)

    fun onRefreshOffers()
    fun onRefreshOrders()
    fun onRefreshBids()
    fun onRefreshProposals()
}

class DefaultProfileChildrenComponent(
    selectedPage : String?,
    componentContext: ComponentContext,
    private val navigationProfile: StackNavigation<ProfileConfig>,
) : ProfileChildrenComponent, ComponentContext by componentContext {

    private val navigationMyOffers = PagesNavigation<MyOfferConfig>()
    private val navigationMyBids = PagesNavigation<MyBidsConfig>()
    private val navigationMyOrders = PagesNavigation<MyOrderConfig>()
    private val navigationMyProposals = PagesNavigation<MyProposalsConfig>()
    private val navigationSettings = PagesNavigation<ProfileSettingsConfig>()

    private val _model = MutableValue(
        ProfileChildrenComponent.Model(
            backHandler = backHandler
        )
    )
    override val model = _model

    private var currentPage = ""

    private var searchID : Long? = null


    init {
        val params = selectedPage?.split("/", limit = 2)

        currentPage = params?.firstOrNull() ?: ""
        val content = params?.lastOrNull()

        when (currentPage) {
            "conversations" -> {
                val mes = if(content != currentPage)content else null
                navigationProfile.replaceAll(ProfileConfig.ConversationsScreen(mes))
            }
            "purchases" -> {
                searchID = content?.toLongOrNull()
                navigationProfile.replaceAll(ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY, content?.toLongOrNull()))
            }
            "sales" -> {
                searchID = content?.toLongOrNull()
                navigationProfile.replaceAll(ProfileConfig.MyOrdersScreen(DealTypeGroup.SELL, content?.toLongOrNull()))
            }
            "proposals" -> {
                navigationProfile.replaceAll(ProfileConfig.MyProposalsScreen)
            }
        }
    }

    override fun onRefreshBids() {
        val index = myBidsPages.value.selectedIndex
        when(myBidsPages.value.items[index].instance){
            is MyBidsComponent -> myBidsPages.value.items[index].instance?.onRefresh()
        }
    }

    override fun onRefreshProposals() {
        val index = myProposalsPages.value.selectedIndex
        when(myProposalsPages.value.items[index].instance){
            is MyProposalsComponent -> myProposalsPages.value.items[index].instance?.onRefresh()
        }
    }

    override fun onRefreshOffers() {
        val index = myOffersPages.value.selectedIndex
        when(myOffersPages.value.items[index].instance){
            is MyOffersComponent -> myOffersPages.value.items[index].instance?.onRefresh()
        }
    }

    override fun onRefreshOrders() {
        val index = myOffersPages.value.selectedIndex
        when(myOrdersPages.value.items[index].instance){
            is MyOrdersComponent -> myOrdersPages.value.items[index].instance?.onRefresh()
        }
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

    override fun selectOfferPage(type: LotsType) {
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
            LotsType.MYBIDLOTS_ACTIVE -> {
                navigationMyBids.select(0)
            }
            LotsType.MYBIDLOTS_UNACTIVE -> {
                navigationMyBids.select(1)
            }
            LotsType.ALL_PROPOSAL -> {
                navigationMyProposals.select(0)
            }
            LotsType.NEED_RESPOSE -> {
                navigationMyProposals.select(1)
            }
            else -> {

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
                        ProfileSettingsConfig(ProfileSettingsTypes.SELLER_SETTINGS),
                        ProfileSettingsConfig(ProfileSettingsTypes.ADDITIONAL_SETTINGS)
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
                    selectDynamicSettings = {
                        goToDynamicSettings(it, null, null)
                    }
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
                itemMyOffers(config, componentContext, navigationProfile,::selectOfferPage)
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
                itemMyBids(config, componentContext, navigationProfile,::selectOfferPage)
            }
        )
    }

    override val myProposalsPages: Value<ChildPages<*, MyProposalsComponent>> by lazy {
        childPages(
            source = navigationMyProposals,
            serializer = MyProposalsConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    listOf(
                        MyProposalsConfig(lotsType = LotsType.ALL_PROPOSAL),
                        MyProposalsConfig(lotsType = LotsType.NEED_RESPOSE),
                    ),
                    selectedIndex = 0,
                )
            },
            key = "ProfileMyProposalsStack",
            childFactory = { config, componentContext ->
                itemMyProposals(config, componentContext, navigationProfile,::selectOfferPage)
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
