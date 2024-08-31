//package application.market.data.database
//
//import application.market.auction_mobile.business.database.RealmHelper
//import io.realm.kotlin.Realm
//import io.realm.kotlin.UpdatePolicy
//import io.realm.kotlin.query.RealmResults
//import io.realm.kotlin.types.RealmObject
//import kotlin.reflect.KClass
//
//class GenericRepository<T : RealmObject>(private val clazz: KClass<T>) {
//    private val realm: Realm by lazy { RealmHelper.getRealmInstance()}
//
//    fun insert(obj: T) {
//        realm.writeBlocking {
//            copyToRealm(obj,UpdatePolicy.ALL)
//        }
//    }
//
//    fun getAll(): RealmResults<T> {
//        return realm.query(clazz).find()
//    }
//
//    fun getByField(fieldName: String, value: Any): RealmResults<T> {
//        return realm.query(clazz, "$fieldName == $0", value).find()
//    }
//
//    fun getByFields(fields: HashMap<String, Any>): RealmResults<T> {
//
//        val conditions =  fields.keys.mapIndexed { index, key -> "$key == \$${index}" }.joinToString(" AND ")
//
//        val values = fields.values.toTypedArray()
//
//        return realm.query(clazz, conditions, *values).find()
//    }
//
//    fun deleteByOwnerObjId(idObject: Any, idOwner: Long) {
//        realm.writeBlocking {
//            val item = query(clazz, "idObject == $0 AND idOwner == $1", idObject, idOwner).first().find()
//            item?.let { delete(it) }
//        }
//    }
//
//    fun deleteByObj(obj: T) {
//        realm.writeBlocking {
//            delete(obj)
//        }
//    }
//
//    fun searchByQuery(query: String,idOwner: Long): RealmResults<T> {
//        val cleanedQuery = query.replace(Regex("""["'`]"""), " ")
//        return realm.query(clazz,"idObject CONTAINS[c] $0 AND idOwner == $1", cleanedQuery, idOwner).find()
//    }
//
//    fun deleteAll() {
//        realm.writeBlocking {
//            val results = query(clazz).find()
//            delete(results)
//        }
//    }
//
//    fun deleteOwnerAll(owner: Long) {
//        realm.writeBlocking {
//            val results = query(clazz, "idOwner == $0", owner).find()
//            delete(results)
//        }
//    }
//
////    fun updateFields(obj: T, fields: HashMap<String, Any>) {
////        realm.writeBlocking {
////            val existingObject = findLatest(obj)
////            existingObject?.let { obj ->
////                fields.forEach { (fieldName, value) ->
////                    val kProperty = obj::class.memberProperties.find { it.name == fieldName }
////                    kProperty?.let { property ->
////                        if (property is KMutableProperty<*>) {
////                            property.setter.call(obj, value)
////                        }
////                    }
////                }
////                copyToRealm(obj, UpdatePolicy.ALL)
////            }
////        }
////    }
////
////    fun updateField(obj: T, fieldName: String, value: Any) {
////        realm.writeBlocking {
////
////            val existingObject = findLatest(obj)
////
////            existingObject?.let { obj ->
////                val kProperty = obj::class.memberProperties.find { it.name == fieldName }
////                kProperty?.let { property ->
////                    if (property is KMutableProperty<*>) {
////                        property.setter.call(obj, value)
////                    }
////                }
////                copyToRealm(obj, UpdatePolicy.ALL)
////            }
////        }
////    }
//
//}
