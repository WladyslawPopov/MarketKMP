package market.engine.core.repositories

import market.engine.core.constants.UserData.login
import market.engine.core.constants.UserData.picUri
import market.engine.core.constants.UserData.token
import market.engine.core.constants.UserData.userInfo
import market.engine.core.network.SAPIRepository

class UserRepository {

    private val sapiRepository: SAPIRepository by lazy {
        SAPIRepository()
    }

    fun updateToken() {
//        val realmRepository = GenericRepository(LoginCache::class)
//        val user = realmRepository.getAll().firstOrNull()

//        if (user != null) {
//            token = user.token
//            login = user.idObject
//            sapiRepository.addHeader("Authorization", user.token)
//        }
    }

    fun delete() {
//        val realmRepository = GenericRepository(LoginCache::class)
//        realmRepository.deleteAll()
        clear()
        sapiRepository.removeHeader("Authorization")

    }

    fun clear(){
        userInfo = null
        picUri = null
        login  = 0
        token = ""
    }
}
