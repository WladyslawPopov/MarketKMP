package market.engine.fragments.root.main.createOrder

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.launch
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.widgets.filterContents.deliveryCardsContents.DeliveryCardsViewModel

interface CreateOrderComponent {

    val additionalModels : Value<AdditionalModel>
    data class AdditionalModel(
        val deliveryCardsViewModel: DeliveryCardsViewModel
    )

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

    private val deliveryCardsViewModel = viewModel("dynamicDeliveryCardViewModel") {
        DeliveryCardsViewModel(createSavedStateHandle())
    }

    override val additionalModels = MutableValue(
        CreateOrderComponent.AdditionalModel(
            deliveryCardsViewModel = deliveryCardsViewModel
        )
    )

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
            createOrderViewModel.scope.launch {
                createOrderViewModel.updateUserInfo()
            }

            if (UserData.token == ""){
                navigateBack()
            }
        }
        lifecycle.doOnDestroy {
            createOrderViewModel.onClear()
            deliveryCardsViewModel.onClear()
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
