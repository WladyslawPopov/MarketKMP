package market.engine.core.constants

import market.engine.core.network.networkObjects.User
import coil3.Uri

object UserData {
    var login : Long = 0
    var picUri : Uri? = null
    var userInfo : User? = null
    var token : String = ""
}
