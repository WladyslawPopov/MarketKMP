package market.engine.fragments.root.main.notificationsHistory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import market.engine.shared.NotificationsHistory

class NotificationsHistoryViewModel(
    val db : MarketDB
) : BaseViewModel() {

    private var _responseGetPage = MutableStateFlow<List<NotificationsHistory>?>(null)
    val responseGetPage : StateFlow<List<NotificationsHistory>?> = _responseGetPage.asStateFlow()

    fun getPage() {
        viewModelScope.launch {
            try {
                val buf = db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
                _responseGetPage.value = buf
            } catch (e : Exception) {
                onError(ServerErrorException("errorDB", e.message.toString()))
            }
        }
    }
}
