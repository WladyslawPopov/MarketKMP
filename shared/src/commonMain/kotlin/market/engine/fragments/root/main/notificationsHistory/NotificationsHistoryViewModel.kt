package market.engine.fragments.root.main.notificationsHistory

import androidx.lifecycle.SavedStateHandle
import kotlinx.serialization.builtins.ListSerializer
import market.engine.core.data.items.NotificationItem
import market.engine.core.network.ServerErrorException
import market.engine.core.repositories.NotificationsRepository
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.printLogD
import market.engine.fragments.base.CoreViewModel
import org.koin.mp.KoinPlatform.getKoin

class NotificationsHistoryViewModel(savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {
    private val notificationsRepository : NotificationsRepository = getKoin().get()

    private var _responseGetPage = savedStateHandle.getSavedStateFlow(
        scope,
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
        setLoading(true)
        try {
            val notificationsFromDb = notificationsRepository.getNotificationList()

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
                            isRead = latestNotification.isRead < 1 || latestNotification.isRead > 1,
                        )
                    }.sortedByDescending { it.timeCreated }
                )
            }

            setLoading(false)
        } catch (e: Exception) {
            onError(ServerErrorException("errorDB", e.message.toString()))
        }
    }

    fun deleteNotification(id: String) {
        notificationsRepository.deleteNotificationById(id)
        getPage()
    }
}
