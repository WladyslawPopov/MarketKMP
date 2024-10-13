package market.engine.presentation.listing

import application.market.agora.business.core.network.functions.OfferOperations
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.globalData.CategoryBaseFilters
import market.engine.core.network.networkObjects.Offer

import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model : Value<Model>
    data class Model(
        val listingViewModel: ListingViewModel
    )
    val globalData : CategoryBaseFilters

    fun onRefresh()

    fun onBackClicked()

    fun goToSearch()

    suspend fun addToFavorites(offer: Offer): Boolean
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
    override val globalData : CategoryBaseFilters = model.value.listingViewModel.globalData

    private val listingViewModel = model.value.listingViewModel
    private val listingData = globalData.listingData
    private val settings = listingViewModel.settings

    private val offerOperations : OfferOperations = getKoin().get()

    init {
        onRefresh()
    }

    override fun onRefresh() {
        listingViewModel.updateCurrentListingData(listingData.copy())
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

    override suspend fun addToFavorites(offer: Offer): Boolean {
        return withContext(Dispatchers.IO) {
//            val buf = offerOperations.postOfferOperationWatch(offer.id)
//            val res = buf.success
//            withContext(Dispatchers.Main) {
//                if (res != null && res.success) {
//                    offer.isWatchedByMe = !offer.isWatchedByMe
//                }
//                offer.isWatchedByMe
//            }
            offer.isWatchedByMe = !offer.isWatchedByMe
            offer.isWatchedByMe
        }
    }
}
