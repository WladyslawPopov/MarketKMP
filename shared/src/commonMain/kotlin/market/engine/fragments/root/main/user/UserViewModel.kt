package market.engine.fragments.root.main.user

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.User
import market.engine.fragments.base.BaseViewModel

class UserViewModel : BaseViewModel() {

    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo : StateFlow<User?> = _userInfo.asStateFlow()

    private val _statusList = MutableStateFlow<ArrayList<String>>(arrayListOf())
    val statusList: StateFlow<ArrayList<String>> = _statusList.asStateFlow()

    val isVisibleUserPanel = mutableStateOf(true)

    private fun initializeUserData(user: User) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    launch { _statusList.value = checkStatusSeller(user.id) }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Initialization error", ""))
            }
        }
    }

    fun getUserInfo(id : Long) {
        viewModelScope.launch {
            try {
                val res =  withContext(Dispatchers.IO){
                    userOperations.getUsers(id)
                }

                withContext(Dispatchers.Main){
                    val user = res.success?.firstOrNull()
                    val error = res.error
                    if (user != null){
                        _userInfo.value = user
                        initializeUserData(user)
                        updateItemTrigger.value++
                    }else{
                        error?.let { throw it }
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
        }
    }
}
