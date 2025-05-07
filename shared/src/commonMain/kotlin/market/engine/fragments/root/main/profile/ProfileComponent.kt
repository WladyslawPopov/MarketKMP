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
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.root.main.profile.navigation.ProfileConfig
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings

interface ProfileComponent {
    val model : Value<Model>

    data class Model(
        val profileViewModel: BaseViewModel,
        val backHandler: BackHandler
    )

    fun goToAllMyOfferListing()
    fun goToAboutMe()
    fun goToSubscribe()
    fun goToSettings(key: String)
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    selectedPage : String?,
    private val navigationProfile: StackNavigation<ProfileConfig>,
    private val navigateToSubscriptions : () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    val viewModel by lazy {  BaseViewModel() }
    private val _model = MutableValue(
        ProfileComponent.Model(
            profileViewModel = viewModel,
            backHandler = backHandler
        )
    )
    override val model = _model

    init {
        lifecycle.doOnResume {
            viewModel.updateUserInfo()
        }
        val params = selectedPage?.split("/", limit = 2)

        val currentPage = params?.firstOrNull() ?: ""
        val content = params?.lastOrNull()

        when (currentPage) {
            "conversations" -> {
                val mes = if(content != currentPage)content else null
                navigationProfile.replaceAll(ProfileConfig.ConversationsScreen(mes))
            }
            "purchases" -> {
                navigationProfile.replaceAll(ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY, content?.toLongOrNull()))
            }
            "sales" -> {
                navigationProfile.replaceAll(ProfileConfig.MyOrdersScreen(DealTypeGroup.SELL, content?.toLongOrNull()))
            }
            "proposals" -> {
                navigationProfile.replaceAll(ProfileConfig.MyProposalsScreen)
            }
            else -> {
                if(isBigScreen.value){
                    navigationProfile.replaceAll(ProfileConfig.MyOffersScreen)
                }else{
                    navigationProfile.replaceAll(ProfileConfig.ProfileScreen(content))
                }
            }
        }

        model.value.profileViewModel.analyticsHelper.reportEvent(
            "view_profile",
            mapOf("user_id" to UserData.login)
        )
    }

    override fun goToAllMyOfferListing() {
        val ld = ListingData()
        val searchData = ld.searchData
        searchData.value.userID = UserData.login
        searchData.value.userLogin = UserData.userInfo?.login
        searchData.value.userSearch = true
        navigationProfile.pushNew(
            ProfileConfig.ListingScreen(ld.data.value, ld.searchData.value, getCurrentDate())
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

    override fun goToSettings(key: String) {
        goToDynamicSettings(key, null, null)
    }
}
