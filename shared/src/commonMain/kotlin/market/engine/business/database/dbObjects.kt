//package application.market.auction_mobile.business.database
//
//import io.realm.kotlin.types.RealmObject
//import io.realm.kotlin.types.annotations.PrimaryKey
//import org.mongodb.kbson.ObjectId
//
//
//class NotificationEntity : RealmObject {
//    @PrimaryKey
//    var id: ObjectId = ObjectId()
//    var idObject: Long = 0
//    var idOwner: Long = 0
//    var title: String = ""
//    var body: String = ""
//    var type: String = ""
//    var timestemp: Long = 0
//    var data : String = ""
//    var isRead: Boolean = false
//}
//
//// User
//class LoginCache : RealmObject {
//    @PrimaryKey
//    var idObject: Long = 0
//    var token: String = ""
//    var data: String = ""
//}
//
//// SearchHistory
//class SearchHistory : RealmObject {
//    @PrimaryKey
//    var id: ObjectId = ObjectId()
//    var idObject: String = ""
//    var idOwner: Long = 0
//}
//
//// LotHistory
//class LotHistory : RealmObject {
//    @PrimaryKey
//    var id: ObjectId = ObjectId()
//    var idObject: Long = 0
//    var idOwner: Long = 0
//}
//
//
//
