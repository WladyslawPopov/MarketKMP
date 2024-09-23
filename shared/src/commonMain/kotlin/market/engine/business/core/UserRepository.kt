package market.engine.business.core

import application.market.agora.business.constants.UserData.login
import application.market.agora.business.constants.UserData.picUri
import application.market.agora.business.constants.UserData.token
import application.market.agora.business.constants.UserData.userInfo
import application.market.agora.business.core.SAPIRepository

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
