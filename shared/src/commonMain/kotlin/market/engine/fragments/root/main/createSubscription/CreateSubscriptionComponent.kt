package market.engine.fragments.root.main.createSubscription

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import org.koin.mp.KoinPlatform.getKoin

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

    private val createSubscriptionViewModel : CreateSubscriptionViewModel = getKoin().get()

    private val _model = MutableValue(
        CreateSubscriptionComponent.Model(
            editId = editId,
            createSubscriptionViewModel = createSubscriptionViewModel,
            backHandler = backHandler
        )
    )

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    override val model = _model

    init {
        createSubscriptionViewModel.getPage(editId)
        analyticsHelper.reportEvent("view_create_subscription", mapOf())
    }

    override fun onBackClicked() {
        navigateBack()
    }
}
