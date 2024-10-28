package market.engine.core.globalData


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import market.engine.core.network.networkObjects.User
import coil3.Uri

object UserData {
    var login : Long = 0
    var picUri : Uri? = null
    var token : String = ""

    var userInfo by mutableStateOf<User?>(null)
        private set // Закрываем setter, чтобы обновлять только через специальные методы

    // Метод обновления userInfo
    fun updateUserInfo(newInfo: User?) {
        userInfo = newInfo
    }
}
