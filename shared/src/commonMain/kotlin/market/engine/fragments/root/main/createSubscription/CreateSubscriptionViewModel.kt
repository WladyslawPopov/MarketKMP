package market.engine.fragments.root.main.createSubscription

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString

data class CreateSubDataState(
    val appBar : SimpleAppBarData = SimpleAppBarData(),
    val fields : List<Fields> = emptyList(),
    val title : String = "",
    val categoryState: CategoryState
)

class CreateSubscriptionViewModel(
    val editId: Long?,
    val component: CreateSubscriptionComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    private val _responseGetFields = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "responseGetFields",
        emptyList(),
        ListSerializer(Fields.serializer())
    )

    private val _openCat = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "openCat",
        false,
        Boolean.serializer()
    )

    val categoryViewModel = CategoryViewModel(
        isFilters = true,
        savedStateHandle = savedStateHandle
    )

    val createSubContentState : StateFlow<CreateSubDataState> = combine(
        _responseGetFields.state,
        _openCat.state,
    ) { getPage, openCat ->
        val defCat = getString(strings.selectCategory)

        val title = getString(
            if (editId == null)
                strings.createNewSubscriptionTitle
            else strings.editLabel
        )

        categoryViewModel.updateFromSearchData(
            SD(
                searchCategoryName = getPage.find { it.key == "category_id" }?.shortDescription ?: defCat,
                searchCategoryID = getPage.find { it.key == "category_id" }?.data?.jsonPrimitive?.longOrNull ?: 1L,
                searchParentID = getPage.find { it.key == "category_id" }?.data?.jsonPrimitive?.longOrNull ?: 1L
            )
        )

        CreateSubDataState(
            appBar = SimpleAppBarData(
                onBackClick = {
                    component.onBackClicked()
                },
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        onClick = {
                            refreshPage()
                        }
                    )
                )
            ),
            fields = getPage,
            title = title,
            categoryState = CategoryState(
                openCategory = openCat,
                categoryViewModel = categoryViewModel
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = CreateSubDataState(
            categoryState = CategoryState(
                openCategory = _openCat.value,
                categoryViewModel = categoryViewModel
            )
        )
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
        _responseGetFields.update { page ->
            page.map {
                if(it.key == "category_id")
                    it.copy(
                        shortDescription = categoryName,
                        data = JsonPrimitive(categoryId)
                    )
                else it.copy()
            }
        }
    }

    fun clearCategory(){
        viewModelScope.launch {
            categoryViewModel.updateFromSearchData(SD())
            _responseGetFields.asyncUpdate { page ->
                page.map {
                    if(it.key == "category_id")
                        it.copy(
                            shortDescription = getString(strings.categoryMain),
                            data = null
                        )
                    else it.copy()
                }
            }
        }
    }

    fun onBack(onBack : () -> Unit){
        if (createSubContentState.value.categoryState.openCategory) {
            if (categoryViewModel.searchData.value.searchCategoryID != 1L) {
                categoryViewModel.navigateBack()
            } else {
                closeCategory()
            }
        }else{
            onBack()
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
                    _responseGetFields.value = payload.fields
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
            _responseGetFields.value.forEach {
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

                            _responseGetFields.update {
                                res.recipe?.fields ?: res.fields
                            }
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

    fun setNewField(field : Fields){
        _responseGetFields.update { oldField ->
            oldField.map {
                if(it.key == field.key)
                    field.copy()
                else
                    it.copy()
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
