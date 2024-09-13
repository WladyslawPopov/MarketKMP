package market.engine.ui.home

import application.market.auction_mobile.business.networkObjects.Category
import application.market.auction_mobile.business.networkObjects.Offer
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import org.koin.mp.KoinPlatform.getKoin
interface HomeComponent {
    val model: Value<Model>

    fun onSearchClicked(id: Long)

    fun goToListing()

    data class Model(
        val categories: StateFlow<List<Category>>,
        val promoOffer1: StateFlow<List<Offer>>,
        val promoOffer2: StateFlow<List<Offer>>,
        val isLoading: StateFlow<Boolean>
    )

    fun onRefresh()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val onSearchSelected: (id: Long) -> Unit,
    private val goToListingSelected: () -> Unit
) : HomeComponent, ComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = getKoin().get()

    private val _model = MutableValue(
        HomeComponent.Model(
            categories = homeViewModel.responseCategory,
            promoOffer1 = homeViewModel.responseOffersPromotedOnMainPage1,
            promoOffer2 = homeViewModel.responseOffersPromotedOnMainPage2,
            isLoading = homeViewModel.isShowProgress
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

    override fun onSearchClicked(id: Long) {
        onSearchSelected(id)
    }

    override fun goToListing() {
        goToListingSelected()
    }

    override fun onRefresh() {
        updateModel()
    }
}


