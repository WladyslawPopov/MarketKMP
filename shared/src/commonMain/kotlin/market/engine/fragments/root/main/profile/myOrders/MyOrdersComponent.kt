package market.engine.fragments.root.main.profile.myOrders

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.DealType
import market.engine.core.network.networkObjects.Offer

interface MyOrdersComponent {
    val model : Value<Model>
    data class Model(
        val viewModel: MyOrdersViewModel,
        var type : DealType,
        val backHandler: BackHandler
    )
    fun goToUser(id : Long)
    fun goToOffer(offer: Offer)
    fun selectMyOrderPage(select : DealType)
    fun goToMessenger(dialogId : Long?)
    fun goToBack()
    fun onRefresh()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultMyOrdersComponent(
    componentContext: JetpackComponentContext,
    val orderSelected: Long? = null,
    val type: DealType,
    val offerSelected: (Long) -> Unit,
    val navigateToMyOrder: (DealType) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToMessenger: (Long?) -> Unit,
    val navigateToBack: () -> Unit
) : MyOrdersComponent, JetpackComponentContext by componentContext {

    private val viewModel : MyOrdersViewModel = MyOrdersViewModel(
        orderSelected,
        type,
        this
    )

    private val _model = MutableValue(
        MyOrdersComponent.Model(
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyOrdersComponent.Model> = _model


    init {
        lifecycle.doOnResume {
            viewModel.updateUserInfo()
            if (UserData.token == ""){
                goToBack()
            }
        }
    }

    override fun goToOffer(offer: Offer) {
        offerSelected(offer.snapshotId)
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

    override fun goToBack() {
        viewModel.onBack{
            navigateToBack()
        }
    }

    override fun onRefresh() {
        viewModel.updatePage()
    }
}
