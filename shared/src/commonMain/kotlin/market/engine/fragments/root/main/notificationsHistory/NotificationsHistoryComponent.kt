package market.engine.fragments.root.main.notificationsHistory

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.common.AnalyticsFactory
import org.koin.mp.KoinPlatform.getKoin

interface NotificationsHistoryComponent {
    val model : Value<Model>

    data class Model(
        val notificationsHistoryViewModel: NotificationsHistoryViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()
}

class DefaultNotificationsHistoryComponent(
    componentContext: ComponentContext,
    val navigateBack: () -> Unit,
) : NotificationsHistoryComponent, ComponentContext by componentContext {

    private val viewModel = NotificationsHistoryViewModel(getKoin().get())

    private val _model = MutableValue(
        NotificationsHistoryComponent.Model(
            notificationsHistoryViewModel = viewModel,
            backHandler = backHandler
        )
    )

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    override val model = _model

    init {
        analyticsHelper.reportEvent("view_notifications_history", mapOf())
    }

    override fun onBackClicked() {
        navigateBack()
    }
}
