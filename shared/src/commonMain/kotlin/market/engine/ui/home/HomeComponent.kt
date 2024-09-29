package market.engine.ui.home

import market.engine.business.core.ServerErrorException
import application.market.agora.business.networkObjects.Category
import application.market.agora.business.networkObjects.Offer
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import org.koin.mp.KoinPlatform.getKoin
interface HomeComponent {
    val model: Value<Model>

    fun onSearchClicked()

    fun goToListing()

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
    private val onSearchSelected: () -> Unit,
    private val goToListingSelected: () -> Unit
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

    override fun onSearchClicked() {
        onSearchSelected()
    }

    override fun goToListing() {
        goToListingSelected()
    }

    override fun onRefresh() {
        updateModel()
    }
}


