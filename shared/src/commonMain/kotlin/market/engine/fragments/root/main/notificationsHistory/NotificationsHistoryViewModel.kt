package market.engine.fragments.root.main.notificationsHistory

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NotificationItem
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deleteReadNotifications
import market.engine.fragments.base.BaseViewModel

class NotificationsHistoryViewModel : BaseViewModel() {

    private var _responseGetPage = MutableStateFlow<List<NotificationItem>?>(null)
    val responseGetPage : StateFlow<List<NotificationItem>?> = _responseGetPage.asStateFlow()

    fun getPage() {
        viewModelScope.launch {
            setLoading(true)
            try {
                db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList().deleteReadNotifications()
                var buf = db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()

                _responseGetPage.value = buildList {
                    addAll(buf.groupBy { it.body to it.data_ }
                        .map { (_, group) ->
                            val latestNotification = group.maxByOrNull { it.timestemp }!!
                            NotificationItem(
                                id = latestNotification.id,
                                type = latestNotification.type,
                                title = latestNotification.title,
                                body = latestNotification.body,
                                data = latestNotification.data_,
                                timeCreated = latestNotification.timestemp,
                                unreadCount = group.count { it.isRead < 1 || it.isRead > 1 },
                                unreadIds = group.filter {
                                    it.isRead < 1 || it.isRead > 1
                                }.map {
                                    it.id
                                },
                                isRead = latestNotification.isRead < 1 || latestNotification.isRead > 1 ,
                            )
                        }.sortedByDescending { it.timeCreated }
                    )
                }

                delay(1000)
                setLoading(false)
            } catch (e : Exception) {
                onError(ServerErrorException("errorDB", e.message.toString()))
            }
        }
    }
}
