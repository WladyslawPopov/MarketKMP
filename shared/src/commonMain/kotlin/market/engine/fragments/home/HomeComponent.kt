package market.engine.fragments.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import market.engine.common.AnalyticsFactory
import market.engine.common.getPermissionHandler
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.filtersObjects.EmptyFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.TopCategory
import market.engine.navigation.main.children.HomeConfig
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
    fun goToCreateOffer()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    val navigation: StackNavigation<HomeConfig>,
    private val navigateToListingSelected: (ListingData, Boolean) -> Unit,
    val navigateToLoginSelected: () -> Unit,
    val navigateToOfferSelected: (id: Long) -> Unit,
    val navigateToCreateOfferSelected: () -> Unit
) : HomeComponent, ComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = getKoin().get()

    private val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    private val _model = MutableValue(
        HomeComponent.Model(
            homeViewModel
        )
    )

    override val model: Value<HomeComponent.Model> = _model

    init {
        updateModel()
        analyticsHelper.reportEvent("view_main_page", mapOf())
        getPermissionHandler().AskPermissionNotification()
    }

    private fun updateModel() {
        homeViewModel.getCategories(listingData = LD(), searchData = SD(), withoutCounter =  true)
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
        val ld = ListingData()
        navigateToListingSelected(ld, true)
    }

    override fun goToCategory(category: TopCategory) {
        val ld = ListingData()
        val searchData = ld.searchData.value

        searchData.searchCategoryID = category.id
        searchData.searchParentID = category.parentId
        searchData.searchCategoryName = category.name
        searchData.searchParentName = category.parentName
        searchData.isRefreshing = true

        navigateToListingSelected(ld, false)
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

            navigateToListingSelected(ld, false)
        }
    }

    override fun goToCreateOffer() {
        navigateToCreateOfferSelected()
    }
}
