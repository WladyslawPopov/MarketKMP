package market.engine.fragments.root.main.profile.myOrders

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.DealType
import market.engine.core.network.networkObjects.Offer
import market.engine.fragments.base.listing.ListingBaseViewModel

interface MyOrdersComponent {

    val additionalModels : Value<AdditionalModels>
    data class AdditionalModels(
        val listingBaseViewModel: ListingBaseViewModel
    )

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

    val listingBaseVM = viewModel("myOrdersBaseViewModel"){
        ListingBaseViewModel(
            savedStateHandle = createSavedStateHandle()
        )
    }

    private val _additionalModels = MutableValue(
        MyOrdersComponent.AdditionalModels(
            listingBaseVM
        )
    )

    override val additionalModels: Value<MyOrdersComponent.AdditionalModels> = _additionalModels

    private val viewModel = viewModel("myOrdersViewModel") {
        MyOrdersViewModel(
            orderSelected,
            type,
            this@DefaultMyOrdersComponent,
            createSavedStateHandle()
        )
    }

    private val _model = MutableValue(
        MyOrdersComponent.Model(
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyOrdersComponent.Model> = _model

    val backCallback = object : BackCallback(){
        override fun onBack() {
            viewModel.onBack{
                navigateToBack()
            }
        }
    }

    init {
        backHandler.register(backCallback)

        lifecycle.doOnResume {
            viewModel.updateUserInfo()
            if (UserData.token == ""){
                navigateToBack()
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

    override fun onRefresh() {
        viewModel.updatePage()
    }
}
