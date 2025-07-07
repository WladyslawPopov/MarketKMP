package market.engine.fragments.root.main.createSubscription

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import market.engine.common.Platform
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.CategoryState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString

data class CreateSubDataState(
    val appBar : SimpleAppBarData = SimpleAppBarData(),
    val page : DynamicPayload<OperationResult>? = null,
    val title : String = "",
    val categoryState: CategoryState = CategoryState()
)

class CreateSubscriptionViewModel(
    val editId: Long?,
    val component: CreateSubscriptionComponent
) : CoreViewModel() {

    private var _responseGetPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    private val _openCat = MutableStateFlow(false)
    val categoryViewModel = CategoryViewModel(
        isFilters = true
    )

    val createSubContentState : StateFlow<CreateSubDataState> = combine(
        _responseGetPage,
        _openCat,
    ) { getPage, openCat ->
        val defCat = getString(strings.selectCategory)

        val title = getString(
            if (editId == null)
                strings.createNewSubscriptionTitle
            else strings.editLabel
        )

        categoryViewModel.updateFromSearchData(
            SD(
                searchCategoryName = getPage?.fields?.find { it.key == "category_id" }?.shortDescription ?: defCat,
                searchCategoryID = getPage?.fields?.find { it.key == "category_id" }?.data?.jsonPrimitive?.longOrNull ?: 1L,
                searchParentID = getPage?.fields?.find { it.key == "category_id" }?.data?.jsonPrimitive?.longOrNull ?: 1L
            )
        )

        CreateSubDataState(
            appBar = SimpleAppBarData(
                onBackClick = {
                    onBack()
                },
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        onClick = {
                            refreshPage()
                        }
                    ),
                )
            ),
            page = getPage,
            title = title,
            categoryState = CategoryState(
                openCategory = openCat,
                categoryViewModel = categoryViewModel
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateSubDataState()
    )

    init {
        getPage(editId)
        analyticsHelper.reportEvent("view_create_subscription", mapOf())
    }

    fun refreshPage(){
        getPage(editId)
        refresh()
    }

    fun applyCategory(categoryName : String, categoryId : Long){
        _responseGetPage.update { page ->
            val newFields = page?.fields?.map {
                if(it.key == "category_id")
                    it.copy(
                        shortDescription = categoryName,
                        data = JsonPrimitive(categoryId)
                    )
                else it.copy()
            } ?: emptyList()

            page?.copy(
                fields = newFields
            )
        }
    }

    fun clearCategory(){
        viewModelScope.launch {
            categoryViewModel.updateFromSearchData(SD())
            _responseGetPage.update { page ->
                val newFields = page?.fields?.map {
                    if(it.key == "category_id")
                        it.copy(
                            shortDescription = getString(strings.categoryMain),
                            data = null
                        )
                    else it.copy()
                } ?: emptyList()

                page?.copy(
                    fields = newFields
                )
            }
        }
    }

    fun onBack(){
        if (createSubContentState.value.categoryState.openCategory) {
            if (categoryViewModel.categoryId.value != 1L) {
                categoryViewModel.navigateBack()
            } else {
                closeCategory()
            }
        }else{
            component.onBackClicked()
        }
    }

    fun getPage(editId : Long?){
        setLoading(true)
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO){
                if(editId == null)
                    operationsMethods.getOperationFields(UserData.login, "create_subscription", "users")
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
            _responseGetPage.value?.fields?.forEach {
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
                        operationsMethods.postOperationFields(UserData.login, "create_subscription", "users", body)
                    else
                        operationsMethods.postOperationFields(editId, "edit_subscription", "subscriptions", body)
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

    fun openCategory(){
        categoryViewModel.initialize()
        _openCat.value = true
    }

    fun closeCategory(){
        _openCat.value = false
    }
}
