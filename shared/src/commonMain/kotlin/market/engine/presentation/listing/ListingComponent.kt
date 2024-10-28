package market.engine.presentation.listing

import application.market.agora.business.core.network.functions.OfferOperations
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.UserRepository

import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model : Value<Model>
    data class Model(
        val listingViewModel: ListingViewModel
    )

    fun onRefresh()

    fun onBackClicked()

    fun goToSearch()

    suspend fun clickOnFavorites(offer: Offer): Boolean
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
    private val offerOperations : OfferOperations = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()


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

    override suspend fun clickOnFavorites(offer: Offer): Boolean {
        return withContext(Dispatchers.IO) {
            val buf = if(!offer.isWatchedByMe) offerOperations.postOfferOperationWatch(offer.id) else
                offerOperations.postOfferOperationUnwatch(offer.id)
            val res = buf.success
            withContext(Dispatchers.Main) {
                if (res != null && res.success) {
                    offer.isWatchedByMe = !offer.isWatchedByMe
                }
                offer.isWatchedByMe
            }
        }
    }
}
