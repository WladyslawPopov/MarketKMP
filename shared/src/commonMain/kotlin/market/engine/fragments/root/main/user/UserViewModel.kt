package market.engine.fragments.root.main.user

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.filtersObjects.ReportFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ReportPageType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.Reports
import market.engine.core.network.networkObjects.User
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.getValue

class UserViewModel : CoreViewModel() {

    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo : StateFlow<User?> = _userInfo.asStateFlow()

    private val _statusList = MutableStateFlow<ArrayList<String>>(arrayListOf())
    val statusList: StateFlow<ArrayList<String>> = _statusList.asStateFlow()

    val isVisibleUserPanel = mutableStateOf(true)

    private val pagingRepository: PagingRepository<Reports> = PagingRepository()

    val listingData = mutableStateOf(ListingData())
    val currentFilter = mutableStateOf("")

    val userOperations : UserOperations by lazy { getKoin().get() }

    fun initFeedback(type : ReportPageType, userId : Long) : Flow<PagingData<Reports>> {

        when(type){
            ReportPageType.ABOUT_ME ->{}
            else -> {
                listingData.value.data.filters = ReportFilters.getByTypeFilter(type)
                listingData.value.data.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
        }
        listingData.value.data.methodServer = "get_public_listing"
        listingData.value.data.objServer = "feedbacks"

        val serializer = Reports.serializer()
        return pagingRepository.getListing(listingData.value, apiService, serializer).cachedIn(viewModelScope)
    }

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

    suspend fun checkStatusSeller(id: Long) : ArrayList<String> {
        val lists = listOf("blacklist_sellers", "blacklist_buyers", "whitelist_buyers")
        val check : ArrayList<String> = arrayListOf()
        for (list in lists) {
            val found = withContext(Dispatchers.IO) {
                userOperations.getUsersOperationsGetUserList(
                    UserData.login,
                    hashMapOf("list_type" to JsonPrimitive(list))
                ).success?.body?.data?.find { it.id == id }
            }

            if (found != null) {
                check.add(list)
            }
        }
        return check
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

    fun addNewSubscribe(
        listingData : LD,
        searchData : SD,
        onSuccess: () -> Unit,
        errorCallback: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = operationsMethods.getOperationFields(
                UserData.login,
                "create_subscription",
                "users"
            )

            val eventParameters : ArrayList<Pair<String, Any?>> = arrayListOf(
                "buyer_id" to UserData.login.toString(),
            )
            analyticsHelper.reportEvent("click_subscribe_query", eventParameters.toMap())

            val body = HashMap<String, JsonElement>()
            response.success?.fields?.forEach { field ->
                when(field.key) {
                    "category_id" -> {
                        if (searchData.searchCategoryID != 1L) {
                            body["category_id"] = JsonPrimitive(searchData.searchCategoryID)
                            eventParameters.add("category_id" to searchData.searchCategoryID.toString())
                        }
                    }
                    "offer_scope" -> {
                        body["offer_scope"] = JsonPrimitive(1)
                    }
                    "search_query" -> {
                        if(searchData.searchString != "") {
                            body["search_query"] = JsonPrimitive(searchData.searchString)
                            eventParameters.add("search_query" to searchData.searchString)
                        }
                    }
                    "seller" -> {
                        if(searchData.userSearch) {
                            body["seller"] = JsonPrimitive(searchData.userLogin)
                            eventParameters.add("seller" to searchData.userLogin.toString())
                        }
                    }
                    "saletype" -> {
                        when (listingData.filters.find { it.key == "sale_type" }?.value) {
                            "buynow" -> {
                                body["saletype"] = JsonPrimitive(0)
                            }
                            "auction" -> {
                                body["saletype"] = JsonPrimitive(1)
                            }
                        }
                        eventParameters.add("saletype" to listingData.filters.find { it.key == "sale_type" }?.value.toString())
                    }
                    "region" -> {
                        listingData.filters.find { it.key == "region" }?.value?.let {
                            if (it != "") {
                                body["region"] = JsonPrimitive(it)
                                eventParameters.add("region" to it)
                            }
                        }
                    }
                    "price_from" -> {
                        listingData.filters.find { it.key == "current_price" && it.operation == "gte" }?.value?.let {
                            if (it != "") {
                                body["price_from"] = JsonPrimitive(it)
                                eventParameters.add("price_from" to it)
                            }
                        }
                    }
                    "price_to" -> {
                        listingData.filters.find { it.key == "current_price" && it.operation == "lte" }?.value?.let {
                            if (it != "") {
                                body["price_to"] = JsonPrimitive(it)
                                eventParameters.add("price_to" to it)
                            }
                        }
                    }
                    else ->{
                        if (field.data != null){
                            body[field.key ?: ""] = field.data!!
                        }
                    }
                }
            }

            val res = operationsMethods.postOperationFields(
                UserData.login,
                "create_subscription",
                "users",
                body
            )

            val buf = res.success
            val err = res.error

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    showToast(
                        successToastItem.copy(
                            message = res.success?.operationResult?.message ?: getString(strings.operationSuccess)
                        )
                    )
                    delay(1000)
                    onSuccess()
                }else {
                    errorCallback(err?.humanMessage ?: "")
                }
            }
        }
    }
}
