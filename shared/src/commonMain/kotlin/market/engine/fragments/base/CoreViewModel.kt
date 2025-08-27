package market.engine.fragments.base


import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
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
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.OperationsMethods
import market.engine.core.network.networkObjects.AdditionalData
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.PayloadExistence
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import market.engine.core.utils.CacheRepository
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.nowAsEpochSeconds
import market.engine.core.utils.parseToOfferItem
import market.engine.shared.AuctionMarketDb
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.getValue
import kotlin.time.Duration.Companion.days

open class CoreViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    val db : AuctionMarketDb by lazy { getKoin().get() }
    val mutex : Mutex by lazy { getKoin().get() }
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

    private val _responseHistory = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseHistory: StateFlow<List<OfferItem>> = _responseHistory.asStateFlow()
    private val _responseOurChoice = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseOurChoice: StateFlow<List<OfferItem>> = _responseOurChoice.asStateFlow()

    val offerOperations : OfferOperations by lazy { getKoin().get() }

    private val _scrollState = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "scroll",
        initialValue = ScrollDataState(),
        serializer = ScrollDataState.serializer()
    )

    val scrollState: StateFlow<ScrollDataState> = _scrollState.state

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
            delay(2000)
            _toastItem.value = ToastItem(message = "", type = ToastType.WARNING, isVisible = false)
        }
    }

    fun getOperationFields(
        id: Long,
        type: String,
        method: String,
        onSuccess: (title: String, List<Fields>) -> Unit
    )
    {
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
    )
    {
        setLoading(true)
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { operationsMethods.postOperationFields(id, type, method, body) }
            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error

                setLoading(false)
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

    fun updateUserInfo()
    {
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

    fun refresh()
    {
        _updatePage.value++
        updateUserInfo()
        onError(ServerErrorException())
        resetScroll()
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
    fun getHistory(currentId: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheRepository = CacheRepository(db, mutex)
                val cacheKey = "viewed_offers"
                val listSerializer = ListSerializer(OfferItem.serializer())

                val cachedOffers = cacheRepository.get(cacheKey, listSerializer)
                if (cachedOffers != null) {
                    _responseHistory.value = cachedOffers
                }

                val queries = db.offerVisitedHistoryQueries
                val historyIds = queries.selectAll(UserData.login).executeAsList()
                    .filter { it != currentId }

                val freshOfferItems = historyIds.mapNotNull { id ->
                    try {
                        offerOperations.getOffer(id).success?.parseToOfferItem()
                    } catch (_: Exception) {
                        null
                    }
                }

                _responseHistory.value = freshOfferItems


                val lifetime = 30.days
                val expirationTimestamp = nowAsEpochSeconds() + lifetime.inWholeSeconds
                cacheRepository.put(cacheKey, freshOfferItems, expirationTimestamp, listSerializer)

                if(currentId == null){
                    getOurChoice(freshOfferItems.lastOrNull()?.id ?: 1L)
                }

            } catch (_: Exception) { }
        }
    }

    fun getOurChoice(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheRepository = CacheRepository(db, mutex)
                val cacheKey = "ourChoice_offers"
                val listSerializer = ListSerializer(OfferItem.serializer())

                val cachedOffers = cacheRepository.get(cacheKey, listSerializer)
                if (cachedOffers != null) {
                    _responseOurChoice.value = cachedOffers
                }

                val response = apiService.getOurChoiceOffers(id)
                val serializer = Payload.serializer(Offer.serializer())
                val ourChoice = deserializePayload(response.payload, serializer).objects
                val freshOffers = ourChoice.map { it.parseToOfferItem() }.toList()

                _responseOurChoice.value = freshOffers

                val lifetime = 30.days
                val expirationTimestamp = nowAsEpochSeconds() + lifetime.inWholeSeconds
                cacheRepository.put(cacheKey, freshOffers, expirationTimestamp, listSerializer)

            } catch (_: Exception) { }
        }
    }
}
