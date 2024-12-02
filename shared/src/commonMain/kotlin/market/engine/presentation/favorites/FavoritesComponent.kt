package market.engine.presentation.favorites

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.common.AnalyticsFactory
import market.engine.core.network.networkObjects.Offer
import org.koin.mp.KoinPlatform.getKoin


interface FavoritesComponent {
    val model : Value<Model>
    data class Model(
        val favViewModel: FavViewModel
    )

    fun goToSubscribes()
    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    val selectedSubscribes : () -> Unit,
    val goToOffer : (Long) -> Unit
) : FavoritesComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        FavoritesComponent.Model(
            favViewModel = getKoin().get()
        )
    )
    override val model: Value<FavoritesComponent.Model> = _model
    override fun goToSubscribes() {
        selectedSubscribes()
    }

    private val favViewModel = model.value.favViewModel
    private val listingData = favViewModel.listingData
    private val searchData = listingData.value.searchData
    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        analyticsHelper.reportEvent("open_favorites", "")
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
        goToOffer(offer.id)
    }
}
