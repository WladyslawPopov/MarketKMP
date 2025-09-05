package market.engine.fragments.base


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
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
import market.engine.core.repositories.CacheRepository
import market.engine.core.repositories.OfferVisitedHistoryRepository
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.nowAsEpochSeconds
import market.engine.core.utils.parseToOfferItem
import market.engine.shared.AuctionMarketDb
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.coroutines.CoroutineContext
import kotlin.getValue
import kotlin.time.Duration.Companion.days

open class CoreViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    val db : AuctionMarketDb by lazy { getKoin().get() }
    val settings : SettingsRepository by lazy { getKoin().get() }
    val apiService by lazy {  getKoin().get<APIService>() }

    private val job = Job()

    private val mainContext: CoroutineContext by lazy { job + Dispatchers.Main }

    val scope: CoroutineScope by lazy { CoroutineScope(mainContext) }
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

    val operationsMethods: OperationsMethods by lazy { getKoin().get() }

    val userRepository: UserRepository by lazy { getKoin().get() }
    val categoryOperations : CategoryOperations by lazy { getKoin().get() }
    val cacheRepository : CacheRepository by lazy { getKoin().get() }

    val offerVisitedHistoryRepository :OfferVisitedHistoryRepository by lazy { getKoin().get() }

    private val _responseHistory = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseHistory: StateFlow<List<OfferItem>> = _responseHistory.asStateFlow()
    private val _responseOurChoice = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseOurChoice: StateFlow<List<OfferItem>> = _responseOurChoice.asStateFlow()

    val offerOperations : OfferOperations by lazy { getKoin().get() }

    private val _scrollState = savedStateHandle.getSavedStateFlow(
        scope = scope,
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
        scope.launch {
            withContext(Dispatchers.IO) {
                delay(2000)
                _toastItem.value =
                    ToastItem(message = "", type = ToastType.WARNING, isVisible = false)
            }
        }
    }

    suspend fun getOperationFields(
        id: Long,
        type: String,
        method: String
    ): Pair<String, List<Fields>>?
    {
        val data = withContext(Dispatchers.IO) {
            operationsMethods.getOperationFields(id, type, method)
        }

        val res = data.success
        val error = data.error

        return if (!res?.fields.isNullOrEmpty()){
            Pair(res.description?:"", res.fields)
        }else{
            if (error != null)
                onError(error)
            null
        }
    }

    suspend fun postOperationFields(
        id: Long,
        type: String,
        method: String,
        body: HashMap<String, JsonElement> = hashMapOf(),
        errorCallback: (List<Fields>?) -> Unit = {}
    ) : Boolean {
        setLoading(true)
        val data = withContext(Dispatchers.IO) {
            operationsMethods.postOperationFields(
                id,
                type,
                method,
                body
            )
        }

        val res = data.success
        val error = data.error

        setLoading(false)
        return if (res != null) {
            if (res.operationResult?.result == "ok") {
                showToast(
                    successToastItem.copy(
                        message = withContext(Dispatchers.IO){
                            getString(
                                strings.operationSuccess
                            )
                        }
                    )
                )
                analyticsHelper.reportEvent(
                    "${type}_success",
                    eventParameters = mapOf(
                        "id" to id,
                    )
                )

                true
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
                        message = withContext(Dispatchers.IO){
                            getString(
                                strings.operationFailed
                            )
                        }
                    )
                )

                errorCallback(res.recipe?.fields ?: res.fields)

                false
            }
        } else {
            if (error != null)
                onError(error)

            false
        }
    }

    suspend fun updateUserInfo() {
        try {
            withContext(Dispatchers.IO) {
                userRepository.updateToken()
                userRepository.updateUserInfo()
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
        } catch (exception: Exception) {
            onError(ServerErrorException(exception.message ?: "Unknown error", ""))
        }
    }

    fun refresh()
    {
        _updatePage.value++
        onError(ServerErrorException())
        resetScroll()
    }

    suspend fun getCategories(
        searchData : SD,
        listingData : LD,
        withoutCounter : Boolean = false
    ) : List<Category> {
         return try {
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

                category
            } else {
                payload.objects
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            emptyList()
        } catch (exception: Exception) {
            onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            emptyList()
        }
    }

    suspend fun postOperationAdditionalData(
        id: Long,
        type: String,
        method: String,
        body: HashMap<String, JsonElement> = hashMapOf(),
    ) : PayloadExistence<AdditionalData>? {
        val data = withContext(Dispatchers.IO) {
            operationsMethods.postOperationAdditionalData(
                id,
                type,
                method,
                body
            )
        }

        val res = data.success
        val error = data.error

        if (res != null) {
            if (res.operationResult?.result != null) {
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

            return res
        } else {
            if (error != null)
                onError(error)

            return null
        }
    }

    suspend fun getHistory(currentId: Long? = null) {
        try {
            val cacheKey = "viewed_offers"
            val listSerializer = ListSerializer(OfferItem.serializer())

            val cachedOffers = cacheRepository.get(cacheKey, listSerializer)
            if (cachedOffers != null) {
                _responseHistory.value = cachedOffers
            }

            val historyIds = offerVisitedHistoryRepository.getHistory().filter { it != currentId }

            val freshOfferItems = withContext(Dispatchers.IO){
                historyIds.mapNotNull { id ->
                    try {
                        offerOperations.getOffer(id).success?.parseToOfferItem()
                    } catch (_: Exception) {
                        null
                    }
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

    suspend fun getOurChoice(id: Long) {
        try {
            val cacheKey = "ourChoice_offers"
            val listSerializer = ListSerializer(OfferItem.serializer())

            val cachedOffers = cacheRepository.get(cacheKey, listSerializer)
            if (cachedOffers != null) {
                _responseOurChoice.value = cachedOffers
            }

            val response = withContext(Dispatchers.IO){
                apiService.getOurChoiceOffers(id)
            }
            val serializer = Payload.serializer(Offer.serializer())
            val ourChoice = deserializePayload(response.payload, serializer).objects
            val freshOffers = ourChoice.map { it.parseToOfferItem() }.toList()

            _responseOurChoice.value = freshOffers

            val lifetime = 30.days
            val expirationTimestamp = nowAsEpochSeconds() + lifetime.inWholeSeconds
            cacheRepository.put(cacheKey, freshOffers, expirationTimestamp, listSerializer)

        } catch (_: Exception) {
        }
    }

    fun onClear() {
        scope.cancel()
    }
}
