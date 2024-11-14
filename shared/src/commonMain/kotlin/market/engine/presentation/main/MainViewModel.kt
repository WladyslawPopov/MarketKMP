package market.engine.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import market.engine.core.items.ToastItem
import market.engine.core.network.APIService
import market.engine.core.types.ToastType
import market.engine.presentation.base.BaseViewModel

class MainViewModel(private val apiService: APIService) : BaseViewModel() {
    private val _events = MutableSharedFlow<UIMainEvent>()
    private val events = _events.asSharedFlow()

    var topBarContent: MutableState<(@Composable () -> Unit)> = mutableStateOf({})
        private set

    var errorContent: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
        private set

    var notFoundContent: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
        private set

    var actionFloatingButton: MutableState<(@Composable () -> Unit)> = mutableStateOf({})
        private set

    var toastItem : MutableState<ToastItem> = mutableStateOf(
        ToastItem(
            message = "",
            type = ToastType.WARNING,
            isVisible = false
        )
    )
        private set


    fun sendEvent(event: UIMainEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun updateTopBar(content: @Composable () -> Unit) {
        topBarContent.value = content
    }

    private fun updateError(content: (@Composable () -> Unit)?) {
        errorContent.value = content
    }

    private fun updateNotFound(content: (@Composable () -> Unit)?) {
        notFoundContent.value = content
    }

    private fun updateToast(toastItem: ToastItem) {
        this.toastItem.value = toastItem
    }

    private fun updateActionFloatingButton(content: (@Composable () -> Unit)) {
        actionFloatingButton.value = content
    }


    init {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UIMainEvent.UpdateTopBar -> updateTopBar(event.content)
                    is UIMainEvent.UpdateError -> updateError(event.content)
                    is UIMainEvent.UpdateNotFound -> updateNotFound(event.content)
                    is UIMainEvent.UpdateToast -> updateToast(event.toastItem)
                    is UIMainEvent.UpdateFloatingActionButton -> updateActionFloatingButton(event.content)
                }
            }
        }
    }
}
sealed class UIMainEvent {
    data class UpdateTopBar(val content: @Composable () -> Unit) : UIMainEvent()
    data class UpdateError(val content: (@Composable () -> Unit)?) : UIMainEvent()
    data class UpdateNotFound(val content: (@Composable () -> Unit)?) : UIMainEvent()
    data class UpdateFloatingActionButton(val content: (@Composable () -> Unit)) : UIMainEvent()
    data class UpdateToast(val toastItem: ToastItem) : UIMainEvent()
}
