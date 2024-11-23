package market.engine.presentation.profileMyOffers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.UserRepository
import market.engine.core.types.LotsType
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform.getKoin


interface MyOffersComponent {
    val model : Value<Model>
    data class Model(
        val viewModel: ProfileMyOffersViewModel,
        var type : LotsType
    )

    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
}

class DefaultMyOffersComponent(
    componentContext: ComponentContext,
    val type: LotsType = LotsType.MYLOT_ACTIVE,
    val offerSelected: (Long) -> Unit,
) : MyOffersComponent, ComponentContext by componentContext {
    private val userRepository = getKoin().get<UserRepository>()

    private val _model = MutableValue(
        MyOffersComponent.Model(
            viewModel = ProfileMyOffersViewModel(
                type,
                getKoin().get(),
            ),
            type = type
        )
    )
    override val model: Value<MyOffersComponent.Model> = _model
    private val analyticsHelper = getKoin().get<AnalyticsHelper>()

    init {
        userRepository.updateUserInfo(model.value.viewModel.viewModelScope)
        analyticsHelper.reportEvent("open_my_offers", "")
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
        if (model.value.viewModel.listingData.value.searchData.value.userSearch || model.value.viewModel.listingData.value.searchData.value.searchString != null){
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
        offerSelected(offer.id)
    }

}
