package market.engine.fragments.root.main.profile.myOffers

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.LotsType
import org.koin.mp.KoinPlatform.getKoin


interface MyOffersComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<Offer>>,
        val viewModel: MyOffersViewModel,
        var type : LotsType
    )

    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
    fun selectMyOfferPage(select : LotsType)
    fun goToCreateOffer(type : CreateOfferType, offerId : Long? = null,  catPath : List<Long>?)
}

class DefaultMyOffersComponent(
    componentContext: ComponentContext,
    val type: LotsType = LotsType.MYLOT_ACTIVE,
    val offerSelected: (Long) -> Unit,
    val selectedMyOfferPage: (LotsType) -> Unit,
    val navigateToCreateOffer: (CreateOfferType, Long?, List<Long>?) -> Unit
) : MyOffersComponent, ComponentContext by componentContext {

    private val viewModel : MyOffersViewModel = MyOffersViewModel(
        type,
        getKoin().get(),
        getKoin().get()
    )

    private val listingData = viewModel.listingData.value

    private val _model = MutableValue(
        MyOffersComponent.Model(
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            type = type
        )
    )
    override val model: Value<MyOffersComponent.Model> = _model
    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        viewModel.updateUserInfo()
        analyticsHelper.reportEvent("open_my_offers", mapOf())
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
        if (listingData.searchData.value.userSearch || listingData.searchData.value.searchString.isNotEmpty()){
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

        lifecycle.doOnResume {
            viewModel.updateItem.value = offer.id
        }
    }

    override fun selectMyOfferPage(select: LotsType) {
        selectedMyOfferPage(select)
    }

    override fun goToCreateOffer(type: CreateOfferType, offerId: Long?, catPath : List<Long>?) {
        navigateToCreateOffer(type, offerId, catPath)

        lifecycle.doOnResume {
            viewModel.updateItem.value = offerId
        }
    }
}
