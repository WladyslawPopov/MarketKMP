package market.engine.core.repositories

import market.engine.core.network.functions.UserOperations
import market.engine.common.AnalyticsFactory
import market.engine.common.notificationIdentifier
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.UserData.clear
import market.engine.core.data.globalData.UserData.login
import market.engine.core.data.globalData.UserData.token

class UserRepository(
    private val sapiRepository: SAPIRepository,
    private val settings : SettingsRepository,
    private val userOperations: UserOperations,
) {
    private val analyticsHelper: AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

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

        //remove screenshots
    }

    suspend fun updateUserInfo(){
        val res = userOperations.getUsers(login)
        if (res.success != null){
                val newInfo = res.success?.firstOrNull()
                if (newInfo != null){
                    if (newInfo.markedAsDeleted){
                        //delete screenshots
                        delete()
                    }else{
                        val userProfileAttributes = mapOf(
                            "name" to newInfo.login,
                            "gender" to newInfo.gender,
                            "userRating" to newInfo.rating.toDouble(),
                            "userVerified" to newInfo.isVerified,
                        )
                        analyticsHelper.setUserProfileID(newInfo.id.toString())
                        analyticsHelper.updateUserProfile(userProfileAttributes)
                        notificationIdentifier(newInfo.id)

                        UserData.updateUserInfo(newInfo)
                    }
                }
            }
    }

}
