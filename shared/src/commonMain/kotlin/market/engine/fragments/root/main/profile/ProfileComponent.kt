package market.engine.fragments.root.main.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.root.main.profile.navigation.ProfileConfig
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.BaseViewModel

interface ProfileComponent {
    val model : Value<Model>

    data class Model(
        val profileViewModel: BaseViewModel,
        val backHandler: BackHandler
    )

    fun goToAllMyOfferListing()
    fun goToAboutMe()
    fun goToSubscribe()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    selectedPage : String?,
    private val navigationProfile: StackNavigation<ProfileConfig>,
    private val navigateToSubscriptions : () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        ProfileComponent.Model(
            profileViewModel = BaseViewModel(),
            backHandler = backHandler
        )
    )
    override val model = _model


    private var currentPage = ""
    private var searchID : Long? = null


    init {
        lifecycle.doOnResume {
            model.value.profileViewModel.updateUserInfo()

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
                "proposals" -> {
                    navigationProfile.replaceAll(ProfileConfig.MyProposalsScreen)
                }
            }

            model.value.profileViewModel.analyticsHelper.reportEvent(
                "view_profile",
                mapOf("user_id" to UserData.login)
            )
        }
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

    override fun goToSubscribe() {
        navigateToSubscriptions()
    }
}
