package market.engine.fragments.root.main.createOrder

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.SelectedBasketItem
import org.koin.mp.KoinPlatform.getKoin

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

class DefaultCreateOrderComponent(
    componentContext: ComponentContext,
    basketItem : Pair<Long, List<SelectedBasketItem>>,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToMyOrders: () -> Unit
) : CreateOrderComponent, ComponentContext by componentContext {

    private val createOrderViewModel : CreateOrderViewModel = CreateOrderViewModel()

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
        createOrderViewModel.loadDeliveryCards()
        createOrderViewModel.getOffers(basketItem.second.map { it.offerId })
        createOrderViewModel.getAdditionalFields(
            basketItem.first,
            basketItem.second.map { it.offerId },
            basketItem.second.map { it.selectedQuantity }
        )

        createOrderViewModel.analyticsHelper.reportEvent("view_create_order", mapOf())
    }

    override fun onBackClicked() {
        navigateBack()
    }

    override fun goToOffer(id: Long) {
        navigateToOffer(id)
    }

    override fun goToSeller(id: Long) {
        navigateToUser(id)
    }
    override fun goToMyOrders() {
        navigateToMyOrders()
    }
}
