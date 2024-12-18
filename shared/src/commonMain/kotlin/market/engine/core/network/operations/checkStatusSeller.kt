package market.engine.core.network.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.UserData
import market.engine.core.network.functions.UserOperations
import org.koin.mp.KoinPlatform.getKoin

suspend fun checkStatusSeller(id: Long) : ArrayList<String> {
    val userOperations : UserOperations = getKoin().get()
    val lists = listOf("blacklist_sellers", "blacklist_buyers", "whitelist_buyers")
    val check : ArrayList<String> = arrayListOf()
    for (list in lists) {
        val found = withContext(Dispatchers.IO) {
            userOperations.getUsersOperationsGetUserList(
                UserData.login,
                hashMapOf("list_type" to list)
            )
                .success?.body?.data?.find { it.id == id }
        }

        if (found != null) {
            check.add(list)
        }
    }
    return check
}
