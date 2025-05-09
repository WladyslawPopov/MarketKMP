package market.engine.fragments.root.main.notificationsHistory

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.items.DeepLink

interface NotificationsHistoryComponent {
    val model : Value<Model>

    data class Model(
        val notificationsHistoryViewModel: NotificationsHistoryViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()
    fun goToDeepLink(url: DeepLink)
}

class DefaultNotificationsHistoryComponent(
    componentContext: ComponentContext,
    val navigateBack: () -> Unit,
    val navigateDeepLink: (DeepLink) -> Unit,
) : NotificationsHistoryComponent, ComponentContext by componentContext {

    private val viewModel = NotificationsHistoryViewModel()

    private val _model = MutableValue(
        NotificationsHistoryComponent.Model(
            notificationsHistoryViewModel = viewModel,
            backHandler = backHandler
        )
    )

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    override val model = _model

    init {
        viewModel.getPage()
        analyticsHelper.reportEvent("view_notifications_history", mapOf())
    }

    override fun onBackClicked() {
        navigateBack()
    }

    override fun goToDeepLink(url: DeepLink) {
        navigateDeepLink(url)
        lifecycle.doOnResume {
            viewModel.getPage()
        }
    }
}
