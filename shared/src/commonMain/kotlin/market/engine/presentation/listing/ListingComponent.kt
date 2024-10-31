package market.engine.presentation.listing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.network.networkObjects.Offer
import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model : Value<Model>
    data class Model(
        val listingViewModel: ListingViewModel
    )

    fun onRefresh()

    fun onBackClicked()

    fun goToSearch()

    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
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
    private val searchData = listingData.searchData
    private val analyticsHelper = getKoin().get<AnalyticsHelper>()

    override fun onRefresh() {
        listingViewModel.firstVisibleItemScrollOffset = 0
        listingViewModel.firstVisibleItemIndex = 0
        listingViewModel.updateCurrentListingData()
        listingViewModel.getOffersRecommendedInListing(listingData.searchData.value.searchCategoryID ?: 1L)

        val eventParameters = mapOf(
            "catalog_category" to searchData.value.searchCategoryName,
            "category_id" to searchData.value.searchCategoryID.toString()
        )
        analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
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

    override fun goToOffer(offer: Offer, isTopPromo : Boolean) {
        if (isTopPromo){
            val eventParameters = mapOf(
                "lot_category" to offer.catpath.lastOrNull(),
                "lot_id" to offer.id,
            )

            analyticsHelper.reportEvent(
                "click_super_top_lots",
                eventParameters
            )
        }
        if (searchData.value.userSearch || searchData.value.searchString != null){
            val eventParameters = mapOf(
                "lot_id" to offer.id,
                "lot_name" to offer.title,
                "lot_city" to offer.freeLocation,
                "auc_delivery" to offer.safeDeal,
                "lot_category" to offer.catpath.lastOrNull(),
                "seller_id" to offer.sellerData?.id,
                "lot_price_start" to offer.currentPricePerItem
            )
            analyticsHelper.reportEvent(
                "click_search_results_item",
                eventParameters
            )
        }else{
            val eventParameters = mapOf(
                "lot_id" to offer.id,
                "lot_name" to offer.title,
                "lot_city" to offer.freeLocation,
                "auc_delivery" to offer.safeDeal,
                "lot_category" to offer.catpath.lastOrNull(),
                "seller_id" to offer.sellerData?.id,
                "lot_price_start" to offer.currentPricePerItem
            )
            analyticsHelper.reportEvent(
                "click_item_at_catalog",
                eventParameters
            )
        }
    }
}
