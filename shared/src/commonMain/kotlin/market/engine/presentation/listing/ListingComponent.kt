package market.engine.presentation.listing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model : Value<Model>
    data class Model(
        val listingViewModel: ListingViewModel
    )

    fun onRefresh()

    fun onBackClicked()

    fun goToSearch()
}

class DefaultListingComponent(
    componentContext: ComponentContext,
    private val searchSelected: () -> Unit,
    private val onBackPressed: () -> Unit
) : ListingComponent, ComponentContext by componentContext {

    private val _model = MutableValue(ListingComponent.Model(
        listingViewModel = getKoin().get()
    ))
    override val model: Value<ListingComponent.Model> = _model
    private val listingViewModel = model.value.listingViewModel

    private val listingData = listingViewModel.listingData

    override fun onRefresh() {
        listingViewModel.firstVisibleItemScrollOffset = 0
        listingViewModel.firstVisibleItemIndex = 0
        listingViewModel.updateCurrentListingData()
        listingViewModel.getOffersRecommendedInListing(listingData.searchData.value.searchCategoryID ?: 1L)
    }

    override fun onBackClicked() {
        if (listingData.searchData.value.searchIsLeaf){
            listingData.searchData.value.searchCategoryID = listingData.searchData.value.searchParentID
            listingData.searchData.value.searchCategoryName = listingData.searchData.value.searchParentName
        }
        listingData.searchData.value.isRefreshing = true
        onBackPressed()
    }

    override fun goToSearch() {
        searchSelected()
    }
}
