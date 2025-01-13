package market.engine.fragments.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
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
        val listingData: ListingData,
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

    private val listingData = ListingData()

    private val _model = MutableValue(
        HomeComponent.Model(
            listingData,
            homeViewModel
        )
    )

    override val model: Value<HomeComponent.Model> = _model

    init {
        updateModel()
        analyticsHelper.reportEvent("view_main_page", mapOf())
        getPermissionHandler().askPermissionNotification()
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
        navigateToListingSelected(listingData, true)
    }

    override fun goToCategory(category: TopCategory) {
        listingData.searchData.value.searchCategoryID = category.id
        listingData.searchData.value.searchParentID = category.parentId
        listingData.searchData.value.searchCategoryName = category.name
        listingData.searchData.value.searchParentName = category.parentName
        listingData.searchData.value.isRefreshing = true

        navigateToListingSelected(listingData, false)
    }

    override fun goToAllPromo() {

        if (listingData.data.value.filters.isEmpty()) {
            listingData.data.value.filters = arrayListOf()
            listingData.data.value.filters.addAll(EmptyFilters.getEmpty())
        }

        model.value.homeViewModel.viewModelScope.launch {
            listingData.data.value.filters.find {
                    filter -> filter.key == "promo_main_page"
            }?.value = "promo_main_page"
            listingData.data.value.filters.find {
                    filter -> filter.key == "promo_main_page"
            }?.interpritation = getString(strings.allPromoOffersBtn)

            listingData.searchData.value.isRefreshing = true

            navigateToListingSelected(listingData, false)
        }
    }

    override fun goToCreateOffer() {
        navigateToCreateOfferSelected()
    }
}
