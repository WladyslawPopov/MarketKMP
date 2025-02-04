package market.engine.fragments.root.main.createSubscription

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import org.koin.mp.KoinPlatform.getKoin

interface CreateNewSubscriptionComponent {
    val model : Value<Model>

    data class Model(
        val editId : Long? = null,
        val createNewSubscriptionViewModel: CreateNewSubscriptionViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()
}

class DefaultCreateNewSubscriptionComponent(
    componentContext: ComponentContext,
    editId : Long?,
    val navigateBack: () -> Unit,
) : CreateNewSubscriptionComponent, ComponentContext by componentContext {

    private val createNewSubscriptionViewModel : CreateNewSubscriptionViewModel = getKoin().get()

    private val _model = MutableValue(
        CreateNewSubscriptionComponent.Model(
            editId = editId,
            createNewSubscriptionViewModel = createNewSubscriptionViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {

    }

    override fun onBackClicked() {
        navigateBack()
    }
}
