package market.engine.fragments.root.main.notificationsHistory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.getDeepLinkByType
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.rows.LazyColumnWithScrollBars

@Composable
fun NotificationsHistoryContent(
    component : NotificationsHistoryComponent,
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.notificationsHistoryViewModel

    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()

    val responseGetPage = viewModel.responseGetPage.collectAsState()
    val toastItem = viewModel.toastItem.collectAsState()

    val error: (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage.isNotBlank()) {
            {
                OnError(err.value)
                {
                    viewModel.onError(ServerErrorException())
                }
            }
        } else {
            null
        }
    }

    val noFound = remember(responseGetPage.value) {
        if (responseGetPage.value?.isEmpty() == true) {
            @Composable {
                NoItemsFoundLayout {
                    viewModel.getPage()
                }
            }
        } else {
            null
        }
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

    EdgeToEdgeScaffold(
        topBar = {
            NotificationsHistoryAppBar(
                onBackClick = onBack,
                onRefresh = {
                    viewModel.getPage()
                }
            )
        },
        onRefresh = {
            viewModel.getPage()
        },
        error = error,
        noFound = noFound,
        isLoading = isLoading.value,
        toastItem = toastItem.value,
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            modifierList = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            items(responseGetPage.value?.size ?: 0, key = { i ->
                responseGetPage.value?.get(i)?.id ?: i
            }) { i->
                responseGetPage.value?.get(i)?.let { item ->
                    NotificationsHistoryItem(item){
                        val link = item.getDeepLinkByType()
                        if (link != null) {
                            component.goToDeepLink(link)
                        }
                    }
                }
            }
        }
    }
}
