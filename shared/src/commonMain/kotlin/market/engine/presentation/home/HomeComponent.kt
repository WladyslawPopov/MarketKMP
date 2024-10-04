package market.engine.presentation.home

import market.engine.core.network.ServerErrorException
import market.engine.core.networkObjects.Category
import market.engine.core.networkObjects.Offer
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import market.engine.core.globalObjects.searchData
import market.engine.presentation.main.HomeConfig
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

    fun onRefresh()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    val navigation: StackNavigation<HomeConfig>,
    val navigateToSearchSelected: () -> Unit,
    val navigateToListingSelected: () -> Unit
) : HomeComponent, ComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = getKoin().get()

    private val _model = MutableValue(
        HomeComponent.Model(
            categories = homeViewModel.responseCategory,
            promoOffer1 = homeViewModel.responseOffersPromotedOnMainPage1,
            promoOffer2 = homeViewModel.responseOffersPromotedOnMainPage2,
            isLoading = homeViewModel.isShowProgress,
            isError = homeViewModel.errorMessage
        )
    )

    override val model: Value<HomeComponent.Model> = _model

    init {
        updateModel()
    }

    private fun updateModel() {
        homeViewModel.getCategory()
        homeViewModel.getOffersPromotedOnMainPage(0, 16)
        homeViewModel.getOffersPromotedOnMainPage(1, 16)
    }

    override fun navigateToSearch() {
        searchData.fromSearch = true
        navigateToSearchSelected()
    }

    override fun navigateToListing() {
        navigateToListingSelected()
    }

    override fun onRefresh() {
        updateModel()
    }
}



