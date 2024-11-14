package market.engine.presentation.profileMyOffers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.globalData.UserData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.UserRepository
import market.engine.core.types.LotsType
import market.engine.presentation.profile.ProfileViewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform.getKoin


interface MyOffersComponent {
    val model : Value<Model>
    data class Model(
        val viewModel: ProfileViewModel,
        var type : LotsType
    )

    fun onRefresh()
    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)

    fun onDestroy()
}

class DefaultMyOffersComponent(
    componentContext: ComponentContext,
    val type: LotsType = LotsType.MYLOT_ACTIVE,
) : MyOffersComponent, ComponentContext by componentContext {
    private val scopeId = "MyOffersScope_$type"
    // Create or get the Koin scope
    private val koinScope: Scope = getKoin().getScopeOrNull(scopeId)
        ?: getKoin().createScope(scopeId, named("MyOffersScope"))

    private val userRepository = getKoin().get<UserRepository>()

    private val _model = MutableValue(
        MyOffersComponent.Model(
            viewModel = koinScope.get(parameters = { parametersOf(type) }),
            type = type
        )
    )
    override val model: Value<MyOffersComponent.Model> = _model
    private val analyticsHelper = getKoin().get<AnalyticsHelper>()

    override fun onRefresh() {

       userRepository.updateUserInfo(model.value.viewModel.viewModelScope)

        model.value.viewModel.updateCurrentListingData(type)
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
        if (model.value.viewModel.listingData.searchData.value.userSearch || model.value.viewModel.listingData.searchData.value.searchString != null){
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

    override fun onDestroy() {
        koinScope.close()
    }
}
