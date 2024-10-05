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
import market.engine.core.globalData.SD
import market.engine.core.types.CategoryScreenType

import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model: Value<Model>

    var lData: ListingData

    data class Model(
        val offers: StateFlow<List<Offer>>,
        val listing: ServerResponse<Flow<PagingData<Offer>>>,
        val isLoading: StateFlow<Boolean>,
    )

    val searchData : StateFlow<SD>

    fun onRefresh()

    fun onBackClicked()

    fun goToSearch()
}

class DefaultListingComponent(
    componentContext: ComponentContext,
    private val searchSelected: () -> Unit,
    private val onBackPressed: () -> Unit
) : ListingComponent, ComponentContext by componentContext {

    init {
        listingData.methodServer = "get_public_listing"
    }

    override val searchData : StateFlow<SD> = getKoin().get()

    override var lData = ListingData(searchData.value.copy(), listingData.copy())

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
        lData = ListingData(searchData.value.copy(), listingData.copy())
        val newListingFlow = listingViewModel.getPage(lData)
        _model.value = _model.value.copy(listing = newListingFlow)
    }

    override fun onRefresh() {
        updateModel()
    }

    override fun onBackClicked() {
        if (searchData.value.searchIsLeaf){
            searchData.value.searchCategoryID = searchData.value.searchParentID
            searchData.value.searchCategoryName = searchData.value.searchParentName
        }
        onBackPressed()
    }

    override fun goToSearch() {
        searchSelected()
    }
}
