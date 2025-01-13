package market.engine.fragments.createOrder

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.koin.mp.KoinPlatform.getKoin

interface CreateOrderComponent {
    val model : Value<Model>

    data class Model(
        val createOrderViewModel: CreateOrderViewModel
    )

    fun onBackClicked()

    fun goToOffer(id : Long)
}

class DefaultCreateOrderComponent(
    componentContext: ComponentContext,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit
) : CreateOrderComponent, ComponentContext by componentContext {

    private val createOrderViewModel : CreateOrderViewModel = getKoin().get()

    private val _model = MutableValue(
        CreateOrderComponent.Model(
            createOrderViewModel = createOrderViewModel
        )
    )

    override val model = _model

    init {

    }

    override fun onBackClicked() {
        navigateBack()
    }

    override fun goToOffer(id: Long) {
        navigateToOffer(id)
    }
}
