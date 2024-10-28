package market.engine.core.repositories

import application.market.agora.business.core.network.functions.UserOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.globalData.UserData
import market.engine.core.globalData.UserData.login
import market.engine.core.globalData.UserData.picUri
import market.engine.core.globalData.UserData.token

class UserRepository(
    private val sapiRepository: SAPIRepository,
    private val settings : SettingsRepository,
    private val userOperations: UserOperations
) {

    fun setToken(l : Long, t : String) {
        settings.setSettingValue("identity", l)
        settings.setSettingValue("token", t)
        login = l
        token = t
        sapiRepository.addHeader("Authorization", token)
    }

    fun updateToken() {
        login = settings.getSettingValue("identity", 1L) ?: 1L
        token = settings.getSettingValue("token", "") ?: ""
        sapiRepository.addHeader("Authorization", token)
    }

    fun delete() {
        settings.setSettingValue("identity", 1L)
        settings.setSettingValue("token", "")
        clear()
        sapiRepository.removeHeader("Authorization")
    }

    fun updateUserInfo(scope : CoroutineScope){
        scope.launch {
            withContext(Dispatchers.IO){
                val res = userOperations.getUsers(login)

                withContext(Dispatchers.Main){
                    if (res.success != null){
                        UserData.updateUserInfo(res.success?.firstOrNull())
                    }
                }
            }
        }
    }

    fun clear(){
        picUri = null
        login  = 0
        token = ""
    }
}
