package market.engine.fragments.profileMyOrders

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import org.koin.mp.KoinPlatform.getKoin


interface MyOrdersComponent {
    val model : Value<Model>
    data class Model(
        val listingData: ListingData,
        val pagingDataFlow : Flow<PagingData<Order>>,
        val viewModel: ProfileMyOrdersViewModel,
        var type : DealType
    )

    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
    fun goToCreateOffer(type : CreateOfferType, offerId : Long? = null,  catPath : List<Long>?)
    fun selectMyOrderPage(select : DealType)
}

class DefaultMyOrdersComponent(
    componentContext: ComponentContext,
    val type: DealType,
    val navigateToCreateOffer: (CreateOfferType, Long?, List<Long>?) -> Unit,
    val offerSelected: (Long) -> Unit,
    val navigateToMyOrder: (DealType) -> Unit,
) : MyOrdersComponent, ComponentContext by componentContext {

    private val viewModel : ProfileMyOrdersViewModel = ProfileMyOrdersViewModel(
        type,
        getKoin().get(),
        getKoin().get()
    )

    val listingData = viewModel.listingData.value

    private val _model = MutableValue(
        MyOrdersComponent.Model(
            listingData = listingData,
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            type = type
        )
    )
    override val model: Value<MyOrdersComponent.Model> = _model
    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        viewModel.updateUserInfo()
        val eventParameters = mapOf(
            "user_id" to UserData.login.toString(),
            "profile_source" to "deals"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
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
            model.value.viewModel.updateItem.value = offer.id
        }
    }

    override fun goToCreateOffer(type: CreateOfferType, offerId: Long?, catPath : List<Long>?) {
        navigateToCreateOffer(type, offerId, catPath)

        lifecycle.doOnResume {
            model.value.viewModel.updateItem.value = offerId
        }
    }

    override fun selectMyOrderPage(select: DealType) {
        navigateToMyOrder(select)
    }
}
