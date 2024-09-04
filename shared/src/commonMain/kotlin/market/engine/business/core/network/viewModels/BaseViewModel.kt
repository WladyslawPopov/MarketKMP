package market.engine.business.core.network.viewModels

import application.market.auction_mobile.business.core.ServerErrorException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class BaseViewModel {

    private val _errorMessage = MutableSharedFlow<ServerErrorException>()
    val errorMessage: SharedFlow<ServerErrorException> = _errorMessage.asSharedFlow()

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    val viewModelScope = CoroutineScope(Dispatchers.Main)

    protected fun onError(exception: ServerErrorException) {
        _isShowProgress.value = false
        viewModelScope.launch {
            _errorMessage.emit(exception)
        }
    }

    protected fun setLoading(isLoading: Boolean) {
        _isShowProgress.value = isLoading
    }
}
