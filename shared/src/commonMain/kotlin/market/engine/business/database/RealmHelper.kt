package market.engine.business.database


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
//    fun getRealmInstance(): Realm {
//        if (realmInstance == null) {
//            realmInstance = Realm.open(config)
//        }
//        return realmInstance ?: Realm.open(config)
//    }
//
//    fun closeRealmInstance() {
//        realmInstance?.close()
//        realmInstance = null
//    }
//}
