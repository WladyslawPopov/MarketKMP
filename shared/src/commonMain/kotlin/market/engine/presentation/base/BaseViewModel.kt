package market.engine.presentation.base

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import market.engine.core.network.ServerErrorException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import market.engine.core.items.ToastItem
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.SettingsRepository
import market.engine.core.types.ToastType
import org.koin.mp.KoinPlatform.getKoin

open class BaseViewModel: ViewModel() {
    private val offersOperations : OfferOperations = getKoin().get()

    suspend fun getUpdatedOfferById(offerId: Long) : Offer? {
        return try {
            val response = offersOperations.getOffer(offerId)
            response.success?.let {
                return it
            }
        } catch (_: Exception) {
            null
        }
    }

    private val _errorMessage = MutableStateFlow(ServerErrorException())
    val errorMessage: StateFlow<ServerErrorException> = _errorMessage.asStateFlow()

    val toastItem = mutableStateOf(ToastItem(message = "", type = ToastType.WARNING, isVisible = false))

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    val viewModelScope = CoroutineScope(Dispatchers.Default)

    protected fun onError(exception: ServerErrorException) {
        _errorMessage.value = exception
    }

    protected fun setLoading(isLoading: Boolean) {
        _isShowProgress.value = isLoading
    }

    fun showToast(newToast: ToastItem) {
        toastItem.value = newToast
        viewModelScope.launch {
            delay(3000)
            toastItem.value = ToastItem(message = "", type = ToastType.WARNING, isVisible = false)
        }
    }

    val settings : SettingsRepository = getKoin().get()
}
