package market.engine.presentation.home

import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import market.engine.common.getPermissionHandler
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.baseFilters.CategoryBaseFilters
import market.engine.core.navigation.configs.HomeConfig
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import org.koin.mp.KoinPlatform.getKoin

interface HomeComponent {
    val model: Value<Model>

    fun navigateToSearch()
    fun navigateToListing()

    data class Model(
        val categories: StateFlow<List<Category>>,
        val promoOffer1: StateFlow<List<Offer>>,
        val promoOffer2: StateFlow<List<Offer>>,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>
    )

    val globalData : CategoryBaseFilters

    fun onRefresh()

    fun goToLogin()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    val navigation: StackNavigation<HomeConfig>,
    val navigateToSearchSelected: () -> Unit,
    val navigateToListingSelected: () -> Unit,
    val navigateToLoginSelected: () -> Unit
) : HomeComponent, ComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = getKoin().get()
    override val globalData : CategoryBaseFilters = getKoin().get()

    private val analyticsHelper : AnalyticsHelper = getKoin().get()

    private val _model = MutableValue(
        HomeComponent.Model(
            categories = homeViewModel.responseCategory,
            promoOffer1 = homeViewModel.responseOffersPromotedOnMainPage1,
            promoOffer2 = homeViewModel.responseOffersPromotedOnMainPage2,
            isLoading = homeViewModel.isShowProgress,
            isError = homeViewModel.errorMessage
        )
    )
    private val userRepository = getKoin().get<UserRepository>()
    private val settingsHelper = getKoin().get<SettingsRepository>()
    override val model: Value<HomeComponent.Model> = _model

    init {
        updateModel()

        userRepository.updateToken()
        userRepository.updateUserInfo(homeViewModel.viewModelScope)

        analyticsHelper.reportEvent("view_main_page", "")

        val isShowReview = settingsHelper.getSettingValue("isShowReview", false) ?: false

        val countLaunch = settingsHelper.getSettingValue("count_launch", 0) ?: 0

        if (countLaunch > 10 && !isShowReview){
            //check review
        }

        getPermissionHandler().AskPermissionNotification()

    }

    private fun updateModel() {
        homeViewModel.getCategory()
        homeViewModel.getOffersPromotedOnMainPage(0, 16)
        homeViewModel.getOffersPromotedOnMainPage(1, 16)
    }

    override fun navigateToSearch() {
        navigateToSearchSelected()
    }

    override fun navigateToListing() {
        navigateToListingSelected()
    }

    override fun onRefresh() {
        userRepository.updateToken()
        userRepository.updateUserInfo(homeViewModel.viewModelScope)

        updateModel()
    }

    override fun goToLogin() {
        navigateToLoginSelected()
    }
}



