package market.engine.presentation.base

import androidx.compose.material.BottomSheetValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import market.engine.core.network.ServerErrorException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.repositories.SettingsRepository
import org.koin.mp.KoinPlatform.getKoin

open class BaseViewModel: ViewModel() {

    private val _errorMessage = MutableStateFlow(ServerErrorException())
    val errorMessage: StateFlow<ServerErrorException> = _errorMessage.asStateFlow()

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    val viewModelScope = CoroutineScope(Dispatchers.Default)

    protected fun onError(exception: ServerErrorException) {
        _errorMessage.value = exception
    }

    protected fun setLoading(isLoading: Boolean) {
        _isShowProgress.value = isLoading
    }

    val settings : SettingsRepository = getKoin().get()

    //scroll positions
    var firstVisibleItemIndex by mutableStateOf(0)
    var firstVisibleItemScrollOffset by mutableStateOf(0)

    var selectItems = mutableStateListOf<Long>()

    //filters params
    var isHideContent = mutableStateOf(false)
    var activeFiltersType = mutableStateOf("")
    val bottomSheetState = mutableStateOf(BottomSheetValue.Collapsed)
}
