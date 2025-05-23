package market.engine.fragments.root.main.profile.profileSettings

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.compressImage
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Choices
import market.engine.core.utils.Base64.encodeToBase64
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class ProfileSettingsViewModel : BaseViewModel() {

    private val _genderSelects = MutableStateFlow<List<Choices>>(emptyList())
    val genderSelects : StateFlow<List<Choices>> = _genderSelects.asStateFlow()

    fun refresh(){
        setLoading(true)
        viewModelScope.launch {
            updateUserInfo()
            delay(2000)
            setLoading(false)
        }
    }

    fun getGenderSelects(){
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO) {
                operationsMethods.getOperationFields(
                    UserData.login,
                    "set_gender",
                    "users",
                )
            }
            val payload = buffer.success
            val resErr = buffer.error
            withContext(Dispatchers.Main) {
                if (payload != null) {
                    payload.fields.firstOrNull()?.choices?.let {
                        _genderSelects.value = it
                    }
                } else {
                    resErr?.let { onError(it) }
                }
            }
        }
    }

    fun setGender(gender : String){
        if (UserData.token != ""){
            val body = HashMap<String, JsonElement>()
            val i = genderSelects.value.find { it.name == gender}
            if (i != null){
                body["new_gender"] = i.code ?: JsonPrimitive(0)
            }

            viewModelScope.launch {
                val buffer = withContext(Dispatchers.IO) {
                    operationsMethods.postOperation(
                        UserData.login,
                        "set_gender",
                        "users",
                        body
                    )
                }

                val res = buffer.success
                val error = buffer.error

                withContext(Dispatchers.Main){
                    if (res != null){
                        updateUserInfo()
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        if (error != null){
                            onError(error)
                        }
                    }
                }
            }
        }
    }

    fun uploadNewAvatar(file : PlatformFile){
        viewModelScope.launch {
            if (UserData.token != "") {
                val barr = file.readBytes()
                val resizeImage = compressImage(barr, 60)
                val body = HashMap<String, JsonElement>()
                body["new_avatar"] = JsonPrimitive(resizeImage.encodeToBase64())

                val buffer = withContext(Dispatchers.IO) {
                    operationsMethods.postOperation(
                        UserData.login,
                        "set_avatar",
                        "users",
                        body
                    )
                }

                val res = buffer.success
                val error = buffer.error

                withContext(Dispatchers.Main) {
                    if (res != null) {
                        updateUserInfo()
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        if (error != null) {
                            onError(error)
                        }
                    }
                }
            }
        }
    }

    fun deleteAvatar(){
        if(UserData.token != "") {
            viewModelScope.launch {
                val buffer = withContext(Dispatchers.IO) {
                    operationsMethods.postOperation(
                        UserData.login,
                        "unset_avatar",
                        "users"
                    )
                }
                val res = buffer.success
                val error = buffer.error

                withContext(Dispatchers.Main) {
                    if (res != null) {
                        updateUserInfo()
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        if (error != null) {
                            onError(error)
                        }
                    }
                }
            }
        }
    }
}
