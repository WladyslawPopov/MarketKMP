package market.engine.fragments.root.main.profile.myOrders

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.DealType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import org.koin.mp.KoinPlatform.getKoin


interface MyOrdersComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<Order>>,
        val viewModel: MyOrdersViewModel,
        var type : DealType
    )
    fun goToUser(id : Long)
    fun goToOffer(offer: Offer)
    fun selectMyOrderPage(select : DealType)
    fun goToMessenger()
}

class DefaultMyOrdersComponent(
    componentContext: ComponentContext,
    val type: DealType,
    val offerSelected: (Long) -> Unit,
    val navigateToMyOrder: (DealType) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToMessenger: () -> Unit
) : MyOrdersComponent, ComponentContext by componentContext {

    private val viewModel : MyOrdersViewModel = MyOrdersViewModel(
        type,
        getKoin().get(),
        getKoin().get()
    )

    val listingData = viewModel.listingData.value

    private val _model = MutableValue(
        MyOrdersComponent.Model(
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

    override fun goToOffer(offer: Offer) {
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
        offerSelected(offer.snapshotId)

        lifecycle.doOnResume {
            viewModel.updateItem.value = offer.snapshotId
        }
    }

    override fun goToUser(id: Long) {
        navigateToUser(id)
    }

    override fun selectMyOrderPage(select: DealType) {
        navigateToMyOrder(select)
    }

    override fun goToMessenger() {
        navigateToMessenger()
    }
}
