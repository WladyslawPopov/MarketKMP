package market.engine.fragments.root.main.notificationsHistory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB

class NotificationsHistoryViewModel(
    val db : MarketDB
) : BaseViewModel() {

    private var _responseGetPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseGetPage : StateFlow<DynamicPayload<OperationResult>?> = _responseGetPage.asStateFlow()

}
