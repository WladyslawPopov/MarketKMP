package market.engine.presentation.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import market.engine.common.AnalyticsFactory
import market.engine.common.getPermissionHandler
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.items.ListingData
import market.engine.core.items.TopCategory
import market.engine.core.navigation.configs.HomeConfig
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

interface HomeComponent {
    val model: Value<Model>
    data class Model(
        val homeViewModel: HomeViewModel
    )

    fun onRefresh()

    fun goToLogin()
    fun goToOffer(id: Long)

    fun goToNewSearch()
    fun goToCategory(category: TopCategory)
    fun goToAllPromo()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    val navigation: StackNavigation<HomeConfig>,
    private val navigateToListingSelected: (ListingData) -> Unit,
    val navigateToLoginSelected: () -> Unit,
    val navigateToOfferSelected: (id: Long) -> Unit
) : HomeComponent, ComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = getKoin().get()

    private val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    private val _model = MutableValue(
        HomeComponent.Model(
            homeViewModel
        )
    )
    private val userRepository = getKoin().get<UserRepository>()
    private val settingsHelper = getKoin().get<SettingsRepository>()
    override val model: Value<HomeComponent.Model> = _model

    init {
        updateModel()

        userRepository.updateToken()
        userRepository.updateUserInfo(homeViewModel.viewModelScope)

        analyticsHelper.reportEvent("view_main_page", mapOf())

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

    override fun onRefresh() {
        userRepository.updateToken()
        userRepository.updateUserInfo(homeViewModel.viewModelScope)

        updateModel()
    }

    override fun goToLogin() {
        navigateToLoginSelected()
    }

    override fun goToOffer(id: Long) {
        navigateToOfferSelected(id)
    }

    override fun goToNewSearch() {
        val ld = ListingData()
        val listingData = ld.data.value
        listingData.isOpenSearch.value = true
        listingData.isOpenCategory.value = false
        navigateToListingSelected(ld)
    }

    override fun goToCategory(category: TopCategory) {
        val ld = ListingData()
        val searchData = ld.searchData.value

        searchData.searchCategoryID = category.id
        searchData.searchParentID = category.parentId
        searchData.searchCategoryName = category.name
        searchData.searchParentName = category.parentName
        searchData.isRefreshing = true
        ld.data.value.isOpenCategory.value = false

        navigateToListingSelected(ld)
    }

    override fun goToAllPromo() {
        val ld = ListingData()
        val searchData = ld.searchData.value
        val listingData = ld.data.value

        if (listingData.filters.isEmpty()) {
            listingData.filters = arrayListOf()
            listingData.filters.addAll(EmptyFilters.getEmpty())
        }

        model.value.homeViewModel.viewModelScope.launch {
            listingData.filters.find {
                    filter -> filter.key == "promo_main_page"
            }?.value = "promo_main_page"
            listingData.filters.find {
                    filter -> filter.key == "promo_main_page"
            }?.interpritation = getString(strings.allPromoOffersBtn)

            searchData.isRefreshing = true
            ld.data.value.isOpenCategory.value = false

            navigateToListingSelected(ld)
        }
    }
}
