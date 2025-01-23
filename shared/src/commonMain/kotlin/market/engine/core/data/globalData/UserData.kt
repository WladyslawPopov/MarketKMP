package market.engine.core.data.globalData


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import market.engine.core.network.networkObjects.User
import coil3.Uri

object UserData {
    var login : Long = 0
    private var picUri : Uri? = null
    var token : String = ""

    var userInfo by mutableStateOf<User?>(null)
        private set // Close setter

    fun updateUserInfo(newInfo: User?) {
        userInfo = newInfo
    }

    fun clear() {
        userInfo = null
        picUri = null
        login = 0
        token = ""
    }
}
