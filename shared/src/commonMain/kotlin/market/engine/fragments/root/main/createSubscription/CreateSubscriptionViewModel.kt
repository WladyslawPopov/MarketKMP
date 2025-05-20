package market.engine.fragments.root.main.createSubscription

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class CreateSubscriptionViewModel : BaseViewModel() {

    private var _responseGetPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseGetPage : StateFlow<DynamicPayload<OperationResult>?> = _responseGetPage.asStateFlow()

    val selectedCategory = mutableStateOf("")
    val selectedCategoryID = mutableStateOf(1L)

    fun getPage(editId : Long?){
        setLoading(true)
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO){
                if(editId == null)
                    userOperations.getUserOperationsCreateSubscription(UserData.login)
                else operationsMethods.getOperationFields(editId, "edit_subscription", "subscriptions")
            }
            val payload = buffer.success
            val resErr = buffer.error
            withContext(Dispatchers.Main){
                if (payload != null){
                    _responseGetPage.value = payload
                }else{
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
                setLoading(false)
            }
        }
    }

    fun postPage(editId : Long?, onSuccess : () -> Unit) {
        viewModelScope.launch {
            setLoading(true)

            val body = HashMap<String, JsonElement>()
            responseGetPage.value?.fields?.forEach {
                if (it.data != null)
                    body[it.key ?: ""] = it.data!!
            }
            val eventParameters = mapOf(
                "user_id" to UserData.login,
                "body" to body
            )

            try {
                val buffer = withContext(Dispatchers.IO) {
                    if (editId == null)
                        operationsMethods.postOperation(UserData.login, "create_subscription", "users", body)
                    else
                        operationsMethods.postOperation(editId, "edit_subscription", "subscriptions", body)
                }

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    val res = buffer.success
                    val resErr = buffer.error
                    if (res != null) {
                        if (res.status == "operation_success") {
                            showToast(
                                successToastItem.copy(
                                    message = getString(strings.operationSuccess)
                                )
                            )

                            if (editId == null)
                                analyticsHelper.reportEvent(
                                    "create_subscription_success",
                                    eventParameters
                                )
                            else
                                analyticsHelper.reportEvent(
                                    "edit_subscription_success",
                                    eventParameters
                                )
                            delay(2000)
                            onSuccess()

                        } else {
                            showToast(
                                errorToastItem.copy(
                                    message = res.recipe?.globalErrorMessage ?: getString(
                                        strings.operationFailed
                                    )
                                )
                            )

                            if (editId == null)
                                analyticsHelper.reportEvent(
                                    "create_subscription_failed",
                                    eventParameters
                                )
                            else
                                analyticsHelper.reportEvent(
                                    "edit_subscription_failed",
                                    eventParameters
                                )


                            _responseGetPage.value = _responseGetPage.value?.copy(
                                fields = res.recipe?.fields ?: res.fields
                            )
                        }
                    } else {
                        if (resErr != null) {
                            onError(resErr)
                        }
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(
                    ServerErrorException(
                        errorCode = exception.message.toString(),
                        humanMessage = exception.message.toString()
                    )
                )
            }
        }
    }
}
