package market.engine.fragments.root.main.notificationsHistory

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.items.DeepLink

interface NotificationsHistoryComponent {
    val model : Value<Model>

    data class Model(
        val notificationsHistoryViewModel: NotificationsHistoryViewModel,
        val backHandler: BackHandler
    )
    fun goToOffer(offerId: Long)
    fun onBackClicked()
    fun goToDeepLink(url: DeepLink)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultNotificationsHistoryComponent(
    componentContext: JetpackComponentContext,
    val navigateBack: () -> Unit,
    val navigateDeepLink: (DeepLink) -> Unit,
    val navigateToOffer: (Long) -> Unit,
) : NotificationsHistoryComponent, JetpackComponentContext by componentContext {

    private val viewModel = viewModel("notificationsHistoryViewModel"){
        NotificationsHistoryViewModel(createSavedStateHandle())
    }

    private val _model = MutableValue(
        NotificationsHistoryComponent.Model(
            notificationsHistoryViewModel = viewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        lifecycle.doOnResume {
            viewModel.getPage()
        }
    }

    override fun goToOffer(offerId: Long) {
        navigateToOffer(offerId)
    }

    override fun onBackClicked() {
        navigateBack()
    }

    override fun goToDeepLink(url: DeepLink) {
        navigateDeepLink(url)
    }
}
