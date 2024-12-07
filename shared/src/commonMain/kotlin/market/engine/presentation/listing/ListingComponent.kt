package market.engine.presentation.listing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.items.ListingData
import market.engine.core.network.networkObjects.Offer
import org.koin.mp.KoinPlatform.getKoin

interface ListingComponent {
    val model : Value<Model>
    data class Model(
        val listingData: ListingData,
        val listingViewModel: ListingViewModel,
    )

    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
    fun goBack()
}

class DefaultListingComponent(
    componentContext: ComponentContext,
    listingData: ListingData,
    private val selectOffer: (Long) -> Unit,
    private val selectedBack: () -> Unit
) : ListingComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        ListingComponent.Model(
            listingData = listingData,
            listingViewModel = getKoin().get()
        )
    )

    override val model: Value<ListingComponent.Model> = _model

    private val searchData = listingData.searchData
    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        val eventParameters = mapOf(
            "catalog_category" to searchData.value.searchCategoryName,
            "category_id" to searchData.value.searchCategoryID.toString()
        )
        analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
        model.value.listingViewModel.init(listingData)
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
        selectOffer(offer.id)

        lifecycle.doOnResume {
            model.value.listingData.data.value.updateItem.value = offer.id
        }
    }

    override fun goBack() {
        selectedBack()
    }
}
