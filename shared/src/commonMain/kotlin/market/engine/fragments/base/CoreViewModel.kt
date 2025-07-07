package market.engine.fragments.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.states.ScrollDataState
import market.engine.core.data.types.ToastType
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.functions.OperationsMethods
import market.engine.core.network.networkObjects.AdditionalData
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.PayloadExistence
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import market.engine.core.utils.deserializePayload
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.shared.MarketDB
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.getValue

open class CoreViewModel : ViewModel() {
    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    val db : MarketDB by lazy { getKoin().get() }
    val settings : SettingsRepository by lazy { getKoin().get() }
    val apiService by lazy {  getKoin().get<APIService>() }

    private val _updatePage = MutableStateFlow(0)
    val updatePage: StateFlow<Int> = _updatePage.asStateFlow()

    private val _errorMessage = MutableStateFlow(ServerErrorException())
    val errorMessage: StateFlow<ServerErrorException> = _errorMessage.asStateFlow()

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    private val _toastItem = MutableStateFlow(ToastItem(message = "", type = ToastType.WARNING, isVisible = false))
    val toastItem = _toastItem.asStateFlow()

    private val _updateItem = MutableStateFlow<Long?>(null)
    val updateItem : StateFlow<Long?> = _updateItem.asStateFlow()

    val viewModelScope = CoroutineScope(Dispatchers.Default)

    val operationsMethods: OperationsMethods by lazy { getKoin().get() }

    val userRepository: UserRepository by lazy { getKoin().get() }
    val categoryOperations : CategoryOperations by lazy { getKoin().get() }


    private val _scrollState = MutableStateFlow(ScrollDataState())
    val scrollState: StateFlow<ScrollDataState> = _scrollState

    fun updateScroll(scrollDataState: ScrollDataState) {
        _scrollState.value = scrollDataState
    }

    fun setUpdateItem(id : Long?){
        _updateItem.value = id
    }

    fun updatePage() {
        _updatePage.value++
    }

    fun setLogoutDialog(show: Boolean) {
        _showLogoutDialog.value = show
    }

    fun resetScroll() {
        _scrollState.value = ScrollDataState()
    }

    fun onError(exception: ServerErrorException) {
        _errorMessage.value = exception
    }

    fun setLoading(isLoading: Boolean) {
        _isShowProgress.value = isLoading
    }

    fun showToast(newToast: ToastItem) {
        _toastItem.value = newToast
        viewModelScope.launch {
            delay(3000)
            _toastItem.value = ToastItem(message = "", type = ToastType.WARNING, isVisible = false)
        }
    }

    fun getOperationFields(id: Long,type: String, method: String, onSuccess: (title: String, List<Fields>) -> Unit){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                operationsMethods.getOperationFields(id, type, method)
            }

            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error

                if (!res?.fields.isNullOrEmpty()){
                    onSuccess(res.description?:"", res.fields)
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun postOperationFields(
        id: Long,
        type: String,
        method: String,
        body: HashMap<String, JsonElement> = hashMapOf(),
        onSuccess: () -> Unit,
        errorCallback: (List<Fields>?) -> Unit
    ){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { operationsMethods.postOperationFields(id, type, method, body) }
            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error
                if (res != null) {
                    if (res.operationResult?.result == "ok") {
                        showToast(
                            successToastItem.copy(
                                message = getString(
                                    strings.operationSuccess
                                )
                            )
                        )
                        analyticsHelper.reportEvent(
                            "${type}_success",
                            eventParameters = mapOf(
                                "id" to id,
                            )
                        )

                        onSuccess()
                    } else {
                        analyticsHelper.reportEvent(
                            "${type}_error",
                            eventParameters = mapOf(
                                "id" to id,
                                "body" to body.toString()
                            )
                        )
                        showToast(
                            errorToastItem.copy(
                                message = getString(
                                    strings.operationFailed
                                )
                            )
                        )

                        errorCallback(res.recipe?.fields ?: res.fields)
                    }
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Unconfined) {
                    userRepository.updateToken()
                    userRepository.updateUserInfo()
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            }
        }
    }

    fun refresh(){
        _updatePage.value++
        updateUserInfo()
        onError(ServerErrorException())
        resetScroll()
    }

    fun addToFavorites(offer : OfferItem, onSuccess: (Boolean) -> Unit) {
        if(UserData.token != "") {
            viewModelScope.launch {
                val buf = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        offer.id,
                        if (offer.isWatchedByMe) "unwatch" else "watch",
                        "offers"
                    )
                }

                val res = buf.success
                withContext(Dispatchers.Main) {
                    if (res != null && res.operationResult?.result == "ok") {
                        val eventParameters = mapOf(
                            "lot_id" to offer.id,
                            "lot_name" to offer.title,
                            "lot_city" to offer.location,
                            "auc_delivery" to offer.safeDeal,
                            "lot_category" to offer.catPath.firstOrNull(),
                            "seller_id" to offer.seller.id,
                            "lot_price_start" to offer.price,
                        )
                        if (!offer.isWatchedByMe) {
                            analyticsHelper.reportEvent("offer_watch", eventParameters)
                        } else {
                            analyticsHelper.reportEvent("offer_unwatch", eventParameters)
                        }

                        updateUserInfo()

                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )

                        onSuccess(!offer.isWatchedByMe)
                    } else {
                        if (buf.error != null)
                            onError(buf.error!!)
                    }
                }
            }
        }else{
            goToLogin(false)
        }
    }

    fun getCategories(
        searchData : SD,
        listingData : LD,
        withoutCounter : Boolean = false,
        onSuccess: (List<Category>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val id = searchData.searchCategoryID
                val response =
                    withContext(Dispatchers.IO) { apiService.getPublicCategories(id) }

                val serializer = Payload.serializer(Category.serializer())
                val payload: Payload<Category> =
                    deserializePayload(response.payload, serializer)

                if (!withoutCounter) {
                    val category = withContext(Dispatchers.IO) {
                        val categoriesWithLotCounts = payload.objects.map { category ->
                            async {
                                val sd = searchData.copy()
                                sd.searchCategoryID = category.id
                                val lotCount =
                                    categoryOperations.getTotalCount(sd, listingData)
                                category.copy(
                                    estimatedActiveOffersCount = lotCount.success ?: 0
                                )
                            }
                        }
                        categoriesWithLotCounts.awaitAll()
                            .filter { it.estimatedActiveOffersCount > 0 }
                    }

                    withContext(Dispatchers.Main) {
                        onSuccess(category)
                    }
                } else {
                    onSuccess(payload.objects)
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            }
        }
    }

    fun postOperationAdditionalData(
        id: Long,
        type: String,
        method: String,
        body: HashMap<String, JsonElement> = hashMapOf(),
        onSuccess: (PayloadExistence<AdditionalData>?) -> Unit
    ) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { operationsMethods.postOperationAdditionalData(id, type, method, body) }
            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error
                if (res != null) {
                    if( res.operationResult?.result != null) {
                        showToast(
                            successToastItem.copy(
                                message = res.operationResult.result!!
                            )
                        )
                    }
                    analyticsHelper.reportEvent(
                        type,
                        eventParameters = mapOf(
                            "id" to id,
                        )
                    )

                    onSuccess(res)
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }
}
