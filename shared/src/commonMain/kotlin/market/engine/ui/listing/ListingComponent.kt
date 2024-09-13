package market.engine.ui.listing

import androidx.paging.PagingData
import market.engine.business.items.ListingData
import application.market.auction_mobile.business.networkObjects.Offer
import application.market.data.globalObjects.listingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import market.engine.business.globalObjects.searchData
import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model: Value<Model>

    val lData: ListingData

    data class Model(
        val offers: StateFlow<List<Offer>>,
        val listing: Flow<PagingData<Offer>>,
        val isLoading: StateFlow<Boolean>,
    )

    fun onRefresh()
}

class DefaultListingComponent(
    componentContext: ComponentContext,
) : ListingComponent, ComponentContext by componentContext {

    override val lData = ListingData(searchData.copy(), listingData.copy())

    private val listingViewModel: ListingViewModel = getKoin().get()

    private val _model = MutableValue(
        ListingComponent.Model(
            offers = listingViewModel.responseOffers,
            listing = listingViewModel.getPage(lData),
            isLoading = listingViewModel.isShowProgress
        )
    )
    
    override val model: Value<ListingComponent.Model> = _model

    private fun updateModel() {
        val newListingFlow = listingViewModel.getPage(lData)
        _model.value = _model.value.copy(listing = newListingFlow)
    }

    override fun onRefresh() {
        updateModel()
    }
}
