package market.engine.presentation.listing

import androidx.paging.PagingData
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.Offer
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import market.engine.core.globalData.CategoryBaseFilters

import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model: Value<Model>

    data class Model(
        val offers: StateFlow<List<Offer>>,
        val listing: ServerResponse<Flow<PagingData<Offer>>>,
        val isLoading: StateFlow<Boolean>,
    )

    val globalData : CategoryBaseFilters

    fun onRefresh()

    fun onBackClicked()

    fun goToSearch()
}

class DefaultListingComponent(
    componentContext: ComponentContext,
    private val searchSelected: () -> Unit,
    private val onBackPressed: () -> Unit
) : ListingComponent, ComponentContext by componentContext {

    override val globalData : CategoryBaseFilters = getKoin().get()

    var listingData = globalData.listingData

    init {
        listingData.data.value.methodServer = "get_public_listing"
    }


    private val listingViewModel: ListingViewModel = getKoin().get()

    private val _model = MutableValue(
        ListingComponent.Model(
            offers = listingViewModel.responseOffers,
            listing = listingViewModel.getPage(listingData),
            isLoading = listingViewModel.isShowProgress
        )
    )

    override val model: Value<ListingComponent.Model> = _model

    private fun updateModel() {
        val newListingFlow = listingViewModel.getPage(listingData)
        _model.value = _model.value.copy(listing = newListingFlow)
    }

    override fun onRefresh() {
        updateModel()
    }

    override fun onBackClicked() {
        if (listingData.searchData.value.searchIsLeaf){
            listingData.searchData.value.searchCategoryID = listingData.searchData.value.searchParentID
            listingData.searchData.value.searchCategoryName = listingData.searchData.value.searchParentName
        }
        onBackPressed()
    }

    override fun goToSearch() {
        searchSelected()
    }
}
