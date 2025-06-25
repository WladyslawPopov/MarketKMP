package market.engine.fragments.root.main.createSubscription

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler

interface CreateSubscriptionComponent {
    val model : Value<Model>

    data class Model(
        val editId : Long? = null,
        val createSubscriptionViewModel: CreateSubscriptionViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()
}

class DefaultCreateSubscriptionComponent(
    componentContext: ComponentContext,
    editId : Long?,
    val navigateBack: () -> Unit,
) : CreateSubscriptionComponent, ComponentContext by componentContext {

    private val createSubscriptionViewModel : CreateSubscriptionViewModel = CreateSubscriptionViewModel(editId, this)

    private val _model = MutableValue(
        CreateSubscriptionComponent.Model(
            editId = editId,
            createSubscriptionViewModel = createSubscriptionViewModel,
            backHandler = backHandler
        )
    )
    override val model = _model

    override fun onBackClicked() {
        navigateBack()
    }
}
