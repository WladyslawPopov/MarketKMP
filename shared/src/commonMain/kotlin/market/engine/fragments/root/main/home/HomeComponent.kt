package market.engine.fragments.root.main.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.launch
import market.engine.common.AnalyticsFactory
import market.engine.common.getPermissionHandler
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.TopCategory
import market.engine.core.network.ServerErrorException
import market.engine.core.repositories.UserRepository
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

interface HomeComponent {
    val model: Value<Model>
    data class Model(
        val listingData: ListingData,
        val homeViewModel: HomeViewModel,
        val backHandler: BackHandler
    )

    fun onRefresh()

    fun goToLogin()
    fun goToOffer(id: Long)
    fun goToNewSearch()
    fun goToCategory(category: TopCategory)
    fun goToAllPromo()
    fun goToCreateOffer()
    fun goToMessenger()
    fun goToContactUs()
    fun goToAppSettings()
    fun goToMyProposals()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    val navigation: StackNavigation<HomeConfig>,
    private val navigateToListingSelected: (ListingData, Boolean) -> Unit,
    val navigateToLoginSelected: () -> Unit,
    val navigateToOfferSelected: (id: Long) -> Unit,
    val navigateToCreateOfferSelected: () -> Unit,
    val navigateToMessengerSelected: () -> Unit,
    val navigateToContactUsSelected: () -> Unit,
    val navigateToSettingsSelected: () -> Unit,
    val navigateToMyProposalsSelected: () -> Unit,
) : HomeComponent, ComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = HomeViewModel()

    private val analyticsHelper : AnalyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    private val userRepository : UserRepository = getKoin().get()

    private val listingData = ListingData()

    private val _model = MutableValue(
        HomeComponent.Model(
            listingData,
            homeViewModel,
            backHandler
        )
    )

    override val model: Value<HomeComponent.Model> = _model

    init {
        userRepository.updateToken()
        updateModel()
        analyticsHelper.reportEvent("view_main_page", mapOf())
        getPermissionHandler().askPermissionNotification()

        lifecycle.doOnResume {
            homeViewModel.updateUserInfo()
        }
    }

    private fun updateModel() {
        homeViewModel.onError(ServerErrorException())
        homeViewModel.getCategories(listingData = LD(), searchData = SD(), withoutCounter =  true){
            homeViewModel.setCategory(it)
        }
        homeViewModel.getOffersPromotedOnMainPage(0, 16)
        homeViewModel.getOffersPromotedOnMainPage(1, 16)
    }

    override fun onRefresh() {
        updateModel()
    }

    override fun goToLogin() {
        navigateToLoginSelected()
    }

    override fun goToOffer(id: Long) {
        navigateToOfferSelected(id)
    }

    override fun goToNewSearch() {
        navigateToListingSelected(listingData, true)
    }

    override fun goToCategory(category: TopCategory) {
        listingData.searchData.value.searchCategoryID = category.id
        listingData.searchData.value.searchParentID = category.parentId
        listingData.searchData.value.searchCategoryName = category.name
        listingData.searchData.value.searchParentName = category.parentName

        navigateToListingSelected(listingData, false)
    }

    override fun goToAllPromo() {
        listingData.data.value.filters = ListingFilters.getEmpty()
        model.value.homeViewModel.viewModelScope.launch {
            val allPromo = getString(strings.allPromoOffersBtn)

            listingData.data.value.filters.find {
                    filter -> filter.key == "promo_main_page"
            }?.value = "promo_main_page"
            listingData.data.value.filters.find {
                    filter -> filter.key == "promo_main_page"
            }?.interpretation = allPromo

            listingData.searchData.value.clear(allPromo)

            navigateToListingSelected(listingData, false)
        }
    }

    override fun goToCreateOffer() {
        navigateToCreateOfferSelected()
    }

    override fun goToMessenger() {
        navigateToMessengerSelected()
    }

    override fun goToContactUs() {
        navigateToContactUsSelected()
    }

    override fun goToAppSettings() {
        navigateToSettingsSelected()
    }

    override fun goToMyProposals() {
        navigateToMyProposalsSelected()
    }
}
