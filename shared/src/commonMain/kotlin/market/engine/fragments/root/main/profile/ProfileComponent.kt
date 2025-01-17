package market.engine.fragments.root.main.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.root.main.profile.navigation.MyOfferConfig
import market.engine.fragments.root.main.profile.navigation.ProfileConfig
import market.engine.fragments.root.main.profile.navigation.itemMyOffers
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.profile.myOffers.MyOffersComponent
import market.engine.fragments.root.main.profile.myOrders.MyOrdersComponent
import market.engine.fragments.root.main.profile.navigation.MyOrderConfig
import market.engine.fragments.root.main.profile.navigation.itemMyOrders
import org.koin.mp.KoinPlatform.getKoin

interface ProfileComponent {
    val model : Value<Model>

    val myOffersPages: Value<ChildPages<*, MyOffersComponent>>

    val myOrdersPages: Value<ChildPages<*, MyOrdersComponent>>

    data class Model(
        val navigationItems: List<NavigationItem>,
        val profileViewModel: ProfileViewModel
    )

    fun updateProfile()
    fun goToAllMyOfferListing()
    fun goToAboutMe()
    fun selectMyOfferPage(type: LotsType)
    fun selectMyOrderPage(type: DealType)
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    val navigationItems: List<NavigationItem>,
    private val navigationProfile: StackNavigation<ProfileConfig>,
    val selectedOrderPage : DealTypeGroup,
) : ProfileComponent, ComponentContext by componentContext {

    private val navigationMyOffers = PagesNavigation<MyOfferConfig>()

    private val navigationMyOrders = PagesNavigation<MyOrderConfig>()


    private val _model = MutableValue(
        ProfileComponent.Model(
            navigationItems = navigationItems,
            profileViewModel = getKoin().get()
        )
    )
    override val model = _model


    init {
        updateProfile()
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
    override val myOrdersPages: Value<ChildPages<*, MyOrdersComponent>> by lazy {
        childPages(
            source = navigationMyOrders,
            serializer = MyOrderConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    when (selectedOrderPage){
                        DealTypeGroup.BUY -> {
                            listOf(
                                MyOrderConfig(
                                    dealType = DealType.BUY_IN_WORK
                                ),
                                MyOrderConfig(
                                    dealType = DealType.BUY_ARCHIVE
                                )
                            )
                        }
                        DealTypeGroup.SELL -> {
                            listOf(
                                MyOrderConfig(
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
                    },
                    selectedIndex = 0,
                )
            },
            key = "ProfileMyOrdersStack",
            childFactory = { config, componentContext ->
                itemMyOrders(
                    config, componentContext, navigationProfile
                ){
                    selectMyOrderPage(it)
                }
            }
        )
    }
}
