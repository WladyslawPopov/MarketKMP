//package application.market.auction_mobile.business.database
//
//import io.realm.kotlin.Realm
//import io.realm.kotlin.RealmConfiguration
//import kotlin.jvm.Synchronized
//
//object RealmHelper {
//    private val config: RealmConfiguration by lazy {
//        RealmConfiguration.
//        Builder(schema =
//            setOf(
//                NotificationEntity::class,
//                LoginCache::class,
//                SearchHistory::class,
//                LotHistory::class
//            )
//        ).schemaVersion(1)
//            .build()
//    }
//
//    private var realmInstance: Realm? = null
//
//    @Synchronized
//    fun getRealmInstance(): Realm {
//        if (realmInstance == null) {
//            realmInstance = Realm.open(config)
//        }
//        return realmInstance ?: Realm.open(config)
//    }
//    @Synchronized
//    fun closeRealmInstance() {
//        realmInstance?.close()
//        realmInstance = null
//    }
//}
