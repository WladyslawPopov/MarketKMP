package market.engine.fragments.root.main.createSubscription

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class CreateSubscriptionViewModel(
    private val userOperations: UserOperations,
    private val subOperations: SubscriptionOperations,
) : BaseViewModel() {

    private var _responseGetPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseGetPage : StateFlow<DynamicPayload<OperationResult>?> = _responseGetPage.asStateFlow()

    fun getPage(editId : Long?){
        setLoading(true)
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO){
                if(editId == null)
                    userOperations.getUserOperationsCreateSubscription(UserData.login)
                else subOperations.getOperationsEditSubscription(editId)
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

    suspend fun postPage(editId : Long?) : Boolean {
        setLoading(true)

        val body = HashMap<String, JsonElement>()
        responseGetPage.value?.fields?.forEach {
            if (it.data != null)
                body[it.key ?: ""] = it.data!!
        }

        try {
            val buffer = withContext(Dispatchers.IO) {
                if (editId == null)
                    userOperations.postUserOperationsCreateSubscription(UserData.login, body)
                else
                    subOperations.postOperationsEditSubscription(editId, body)
            }

            return withContext(Dispatchers.Main) {
                val res = buffer.success
                val resErr = buffer.error
                if (res != null) {
                    val serializer = DynamicPayload.serializer(OperationResult.serializer())
                    val payload: DynamicPayload<OperationResult> = deserializePayload(res.payload, serializer)
                    if (payload.status == "operation_success") {
                        showToast(
                            ToastItem(
                                isVisible = true,
                                message = getString(strings.operationSuccess),
                                type = ToastType.SUCCESS
                            )
                        )
                        return@withContext true

                    } else {
                        showToast(
                            ToastItem(
                                isVisible = true,
                                message = payload.recipe?.globalErrorMessage ?: getString(
                                    strings.operationFailed
                                ),
                                type = ToastType.ERROR
                            )
                        )
                        _responseGetPage.value = _responseGetPage.value?.copy(
                            fields = payload.recipe?.fields ?: payload.fields
                        )
                        return@withContext false
                    }
                }else{
                    if (resErr != null) {
                        onError(resErr)
                    }
                    return@withContext false
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            return false
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            return false
        } finally {
            setLoading(false)
        }
    }
}
