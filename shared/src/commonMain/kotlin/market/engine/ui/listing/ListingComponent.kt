package market.engine.ui.listing

import application.market.auction_mobile.business.networkObjects.Offer
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model: Value<Model>

    data class Model(
        val offers: StateFlow<List<Offer>>,
        val isLoading: StateFlow<Boolean>
    )

    fun onRefresh()
}

class DefaultListingComponent(
    componentContext: ComponentContext,
) : ListingComponent, ComponentContext by componentContext {

    private val listingViewModel: ListingViewModel = getKoin().get()

    private val _model = MutableValue(
        ListingComponent.Model(
            offers = listingViewModel.responseOffers,
            isLoading = listingViewModel.isShowProgress
        )
    )

    override val model: Value<ListingComponent.Model> = _model

    init {
        updateModel()
    }

    private fun updateModel() {
        listingViewModel.getPage("")
    }

    override fun onRefresh() {
        updateModel()
    }
}
