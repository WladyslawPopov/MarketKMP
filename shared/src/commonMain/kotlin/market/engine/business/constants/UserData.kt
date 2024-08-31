package application.market.auction_mobile.business.constants

import application.market.auction_mobile.business.networkObjects.User
import coil3.Uri

object UserData {
    var login : Long = 0
    var picUri : Uri? = null
    var userInfo : User? = null
    var token : String = ""
}
