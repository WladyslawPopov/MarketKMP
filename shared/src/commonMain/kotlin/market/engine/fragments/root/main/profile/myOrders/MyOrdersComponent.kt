package market.engine.fragments.root.main.profile.myOrders

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.DealType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order


interface MyOrdersComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<Order>>,
        val viewModel: MyOrdersViewModel,
        var type : DealType,
        val backHandler: BackHandler
    )
    fun goToUser(id : Long)
    fun goToOffer(offer: Offer)
    fun selectMyOrderPage(select : DealType)
    fun goToMessenger(dialogId : Long?)
    fun updateItem(oldOrder : Order?)
    fun goToBack()
    fun onRefresh()
}

class DefaultMyOrdersComponent(
    componentContext: ComponentContext,
    val orderSelected: Long? = null,
    val type: DealType,
    val offerSelected: (Long) -> Unit,
    val navigateToMyOrder: (DealType) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToMessenger: (Long?) -> Unit,
    val navigateToBack: () -> Unit
) : MyOrdersComponent, ComponentContext by componentContext {

    private val viewModel : MyOrdersViewModel = MyOrdersViewModel(
        orderSelected,
        type,
    )

    val listingData = viewModel.listingData.value

    private val _model = MutableValue(
        MyOrdersComponent.Model(
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyOrdersComponent.Model> = _model


    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            viewModel.updateUserInfo()
            if (UserData.token == ""){
                goToBack()
            }
        }
        val eventParameters = mapOf(
            "user_id" to UserData.login.toString(),
            "profile_source" to "deals"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
    }

    override fun goToOffer(offer: Offer) {
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

    override fun goToMessenger(dialogId : Long?) {
        navigateToMessenger(dialogId)
    }

    override fun updateItem(oldOrder: Order?) {
        viewModel.viewModelScope.launch {
            val buf = withContext(Dispatchers.IO) {
                viewModel.updateItem(viewModel.updateItem.value)
            }

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    oldOrder?.owner = buf.owner
                    oldOrder?.trackId = buf.trackId
                    oldOrder?.marks = buf.marks
                    oldOrder?.feedbacks = buf.feedbacks
                    oldOrder?.comment = buf.comment
                    oldOrder?.paymentMethod = buf.paymentMethod
                    oldOrder?.deliveryMethod = buf.deliveryMethod
                    oldOrder?.deliveryAddress = buf.deliveryAddress
                    oldOrder?.dealType = buf.dealType
                    oldOrder?.lastUpdatedTs = buf.lastUpdatedTs
                    viewModel.updateItem.value = null
                    viewModel.updateItemTrigger.value++
                }else {
                    oldOrder?.owner = 1L
                    viewModel.updateItem.value = null
                    viewModel.updateItemTrigger.value++
                }
            }
        }
    }

    override fun goToBack() {
        navigateToBack()
    }

    override fun onRefresh() {
        viewModel.onRefresh()
    }
}
