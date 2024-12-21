package market.engine.fragments.profile

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
import market.engine.navigation.main.children.profile.MyOfferConfig
import market.engine.navigation.main.children.profile.ProfileConfig
import market.engine.navigation.main.children.profile.itemMyOffers
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.profileMyOffers.MyOffersComponent
import org.koin.mp.KoinPlatform.getKoin

interface ProfileComponent {
    val model : Value<Model>

    val myOffersPages: Value<ChildPages<*, MyOffersComponent>>

    data class Model(
        val navigationItems: List<NavigationItem>,
        val profileViewModel: ProfileViewModel
    )
    fun updateProfile()
    fun goToAllMyOfferListing()
    fun goToAboutMe()
    fun selectMyOfferPage(type: LotsType)
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    val navigationItems: List<NavigationItem>,
    private val navigationProfile: StackNavigation<ProfileConfig>,
) : ProfileComponent, ComponentContext by componentContext {

    private val navigationMyOffers = PagesNavigation<MyOfferConfig>()

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
                        MyOfferConfig(type = LotsType.MYLOT_ACTIVE),
                        MyOfferConfig(type = LotsType.MYLOT_UNACTIVE),
                        MyOfferConfig(type = LotsType.MYLOT_FUTURE)
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
}
