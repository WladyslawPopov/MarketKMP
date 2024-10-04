package market.engine.presentation.listing

import androidx.paging.PagingData
import market.engine.core.network.ServerResponse
import market.engine.core.items.ListingData
import market.engine.core.networkObjects.Offer
import market.engine.core.globalObjects.listingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import market.engine.core.globalObjects.searchData
import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model: Value<Model>

    var lData: ListingData

    data class Model(
        val offers: StateFlow<List<Offer>>,
        val listing: ServerResponse<Flow<PagingData<Offer>>>,
        val isLoading: StateFlow<Boolean>,
    )

    fun onRefresh()

    fun onBackClicked()
}

class DefaultListingComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit
) : ListingComponent, ComponentContext by componentContext {

    init {
        listingData.methodServer = "get_public_listing"
    }

    override var lData = ListingData(searchData.copy(), listingData.copy())

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
        searchData.clear()
        lData = ListingData(searchData.copy(), listingData.copy())
        val newListingFlow = listingViewModel.getPage(lData)
        _model.value = _model.value.copy(listing = newListingFlow)
    }

    override fun onRefresh() {
        updateModel()
    }

    override fun onBackClicked() {
        onBackPressed()
    }
}
