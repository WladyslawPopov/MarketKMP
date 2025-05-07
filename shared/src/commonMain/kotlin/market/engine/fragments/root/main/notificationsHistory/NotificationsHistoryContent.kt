package market.engine.fragments.root.main.notificationsHistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout

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
        viewModel.getPage()
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
        {
            onError(err)
            {
                viewModel.onError(ServerErrorException())
            }
        }
    } else {
        null
    }

    val noFound = if (responseGetPage.value?.isEmpty() == true){
            @Composable {
                showNoItemLayout {
                    refresh()
                }
            }
        }else{
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
        noFound = noFound,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn {
                items(responseGetPage.value?.size ?: 0, key = { i ->
                    responseGetPage.value?.get(i)?.id ?: i
                }) { i->
                    responseGetPage.value?.get(i)?.let { item ->
                        Row {
                            Column {
                                Text(item.title)
                                Text(item.body)
                            }
                        }
                    }
                }
            }
        }
    }
}
