package market.engine.fragments.root.main.listing

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.utils.printLogD
import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model : Value<Model>
    data class Model(
        var pagingDataFlow : Flow<PagingData<Offer>>,
        val listingViewModel: ListingViewModel,
        val backHandler: BackHandler
    )

    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
    fun goBack()
    fun goToSubscribe()
}

class DefaultListingComponent(
    isOpenCategory : Boolean,
    isOpenSearch : Boolean,
    componentContext: ComponentContext,
    listingData: ListingData,
    private val selectOffer: (Long) -> Unit,
    private val selectedBack: () -> Unit,
    private val navigateToSubscribe: () -> Unit,
) : ListingComponent, ComponentContext by componentContext {

    private val listingViewModel : ListingViewModel = ListingViewModel(getKoin().get())

    private val _model = MutableValue(
        ListingComponent.Model(
            pagingDataFlow = listingViewModel.init(listingData),
            listingViewModel = listingViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<ListingComponent.Model> = _model

    private val searchData = listingData.searchData.value
    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        val eventParameters = mapOf(
            "catalog_category" to searchData.searchCategoryName,
            "category_id" to searchData.searchCategoryID.toString()
        )
        analyticsHelper.reportEvent("open_catalog_listing", eventParameters)

        if(isOpenCategory)
            listingViewModel.activeFiltersType.value = "categories"

        listingViewModel.isOpenSearch.value = isOpenSearch
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

        if (searchData.userSearch || searchData.searchString.isNotEmpty()){
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
        selectOffer(offer.id)

        lifecycle.doOnResume {
            listingViewModel.updateItem.value = offer.id
            printLogD("Update item Listing", offer.id.toString())
        }
    }

    override fun goBack() {
        selectedBack()
    }

    override fun goToSubscribe() {
        navigateToSubscribe()
    }
}
