package market.engine.fragments.root.main.profile

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
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
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings

interface ProfileComponent {
    val model : Value<Model>

    data class Model(
        val profileViewModel: CoreViewModel,
        val backHandler: BackHandler
    )

    fun goToAllMyOfferListing()
    fun goToAboutMe()
    fun goToSubscribe()
    fun goToSettings(key: String)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultProfileComponent(
    componentContext: JetpackComponentContext,
    selectedPage : String?,
    private val navigationProfile: StackNavigation<ProfileConfig>,
    private val navigateToSubscriptions : () -> Unit
) : ProfileComponent, JetpackComponentContext by componentContext {

    val viewModel = viewModel("rootViewModel"){
        CoreViewModel(createSavedStateHandle())
    }

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
                navigationProfile.pushNew(ProfileConfig.ConversationsScreen(mes))
            }
            "purchases" -> {
                navigationProfile.pushNew(ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY, content?.toLongOrNull()))
            }
            "sales" -> {
                navigationProfile.pushNew(ProfileConfig.MyOrdersScreen(DealTypeGroup.SELL, content?.toLongOrNull()))
            }
            "proposals" -> {
                navigationProfile.pushNew(ProfileConfig.MyProposalsScreen)
            }
            else -> {
                if(isBigScreen.value){
                    navigationProfile.pushNew(ProfileConfig.MyOffersScreen)
                }else{
                    navigationProfile.pushNew(ProfileConfig.ProfileScreen(content))
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
        searchData.userID = UserData.login
        searchData.userLogin = UserData.userInfo?.login
        searchData.userSearch = true
        navigationProfile.pushNew(
            ProfileConfig.ListingScreen(ld.data, ld.searchData, getCurrentDate())
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
