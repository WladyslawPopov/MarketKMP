package market.engine.fragments.root.main.createOrder

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.SelectedBasketItem

interface CreateOrderComponent {
    val model : Value<Model>

    data class Model(
        val basketItem : Pair<Long, List<SelectedBasketItem>>,
        val createOrderViewModel: CreateOrderViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()

    fun goToOffer(id : Long)
    fun goToSeller(id : Long)
    fun goToMyOrders()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultCreateOrderComponent(
    componentContext: JetpackComponentContext,
    basketItem : Pair<Long, List<SelectedBasketItem>>,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToMyOrders: () -> Unit
) : CreateOrderComponent, JetpackComponentContext by componentContext {

    private val createOrderViewModel = viewModel("createOrderViewModel") {
        CreateOrderViewModel(basketItem, this@DefaultCreateOrderComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        CreateOrderComponent.Model(
            basketItem = basketItem,
            createOrderViewModel = createOrderViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        lifecycle.doOnResume {
            createOrderViewModel.updateUserInfo()

            if (UserData.token == ""){
                navigateBack()
            }
        }
    }

    override fun onBackClicked() {
        navigateBack()
    }

    override fun goToOffer(id: Long) {
        navigateBack()
        navigateToOffer(id)
    }

    override fun goToSeller(id: Long) {
        navigateBack()
        navigateToUser(id)
    }
    override fun goToMyOrders() {
        navigateToMyOrders()
    }
}
