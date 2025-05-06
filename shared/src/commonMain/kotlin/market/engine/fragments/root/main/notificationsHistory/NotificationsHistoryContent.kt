package market.engine.fragments.root.main.notificationsHistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError

@Composable
fun NotificationsHistoryContent(
    component : NotificationsHistoryComponent,
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.notificationsHistoryViewModel

    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()

    val responseGetPage = viewModel.responseGetPage.collectAsState()

    val refresh = {
        viewModel.onError(ServerErrorException())
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
        {
            onError(err)
            {
                refresh()
            }
        }
    } else {
        null
    }

    val onBack = {
        component.onBackClicked()
    }

    BackHandler(
        backHandler = model.value.backHandler,
        onBack = {
            onBack()
        }
    )

    BaseContent(
        topBar = {
            NotificationsHistoryAppBar(
                onBackClick = onBack,
                onRefresh = refresh
            )
        },
        onRefresh = {
            refresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()

    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {

        }
    }
}
