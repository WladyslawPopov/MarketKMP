package market.engine.fragments.root.main.notificationsHistory

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NotificationItem
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deleteReadNotifications
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.printLogD
import market.engine.fragments.base.CoreViewModel

class NotificationsHistoryViewModel(savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {

    private var _responseGetPage = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "responseGetPage",
        emptyList(),
        ListSerializer(NotificationItem.serializer())
    )
    val responseGetPage = _responseGetPage.state

    init {
        getPage()

        analyticsHelper.reportEvent("view_notifications_history", mapOf())
    }

    fun getPage() {
        refresh()
        viewModelScope.launch {
            setLoading(true)
            try {
                val notificationsFromDb = mutex.withLock {
                    deleteReadNotifications(db, mutex)
                    db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
                }
                _responseGetPage.value = buildList {
                    addAll(notificationsFromDb.groupBy { it.body to it.data_ }
                        .map { (_, group) ->
                            val latestNotification = group.maxByOrNull { it.timestemp }!!

                            printLogD("NotificationsHistoryViewModel", "getPage: $latestNotification")

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

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            mutex.withLock {
                db.notificationsHistoryQueries.deleteNotificationById(id)
            }
            getPage()
        }
    }
}
