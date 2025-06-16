package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.ui.text.AnnotatedString
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.SubItemEvents
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.MenuItem
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.states.SubContentState
import market.engine.core.data.states.SubItemState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class SubViewModel(component: SubscriptionsComponent) : BaseViewModel()
{
    private val subscriptionOperations: SubscriptionOperations = getKoin().get()

    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData())

    private val _activeWindowType = MutableStateFlow(ActiveWindowListingType.LISTING)

    val deleteId = MutableStateFlow(1L)
    val titleDialog = MutableStateFlow(AnnotatedString(""))

    val pagingParamsFlow: Flow<ListingData> = combine(
        _listingData,
        updatePage
    ) { listingData, _ ->
        listingData
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<SubItemState>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Subscription.serializer()
            ){ tc ->
                totalCount.update {
                    tc
                }
            }.map { pagingData ->
                pagingData.map { subscription ->
                    SubItemState(
                        subscription = subscription,
                        events = SubItemEventsImpl(
                            component = component,
                            viewModel = this,
                            sub = subscription
                        )
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PagingData.empty()
        ).cachedIn(viewModelScope)

    val subContentState: StateFlow<SubContentState> = combine(
        _listingData,
        _activeWindowType
    ){ listingData, activeType ->
        SubContentState(
            listingBaseState = ListingBaseState(
                columns = if (isBigScreen.value) 2 else 1,
                activeWindowType = activeType
            ),
            appState = SimpleAppBarData(
                onBackClick = {

                }
            ),
            listingData = listingData,
            activeFilterListingBtnItem = FilterListingBtnItem(
                text = listingData.data.sort?.interpretation ?: "",
                removeFilter = {
                    _listingData.update {
                        it.copy(
                            data = it.data.copy(
                                sort = null
                            )
                        )
                    }
                    refresh()
                },
                itemClick = {
                    _activeWindowType.value = ActiveWindowListingType.SORTING
                }
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SubContentState()
    )

    init {
        _listingData.update {
            it.copy(
                data = it.data.copy(
                    methodServer = "get_cabinet_listing",
                    objServer = "subscriptions",
                )
            )
        }
    }

    fun getSubscription(subId : Long, onSuccess : (Subscription?) -> Unit ) {
         viewModelScope.launch {
             val buffer = withContext(Dispatchers.IO) {
                 subscriptionOperations.getSubscription(
                     subId
                 )
             }
             withContext(Dispatchers.Main) {
                 val res = buffer.success
                 onSuccess(res)
             }
         }
    }

    fun enableSubscription(subId : Long) {
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    subId,
                    "enable_subscription",
                    "subscriptions"
                )
            }
            val res = buffer.success
            val resError = buffer.error
            withContext(Dispatchers.Main) {
                if (res != null) {
                    updateItem.value = subId
                } else {
                    if (resError != null) {
                        onError(resError)
                    }
                }
            }
        }
    }

    fun disableSubscription(subId : Long) {
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    subId,
                    "disable_subscription",
                    "subscriptions"
                )
            }
            val res = buffer.success
            val resError = buffer.error

            withContext(Dispatchers.Main) {
                if (res != null) {
                    updateItem.value = subId
                } else {
                    if (resError != null) {
                        onError(resError)
                    }
                }
            }
        }
    }

    fun backClick(){
        when{
            _activeWindowType.value != ActiveWindowListingType.LISTING ->{
                _activeWindowType.value = ActiveWindowListingType.LISTING
            }
            else -> {

            }
        }
    }

    fun openSort(){
        _activeWindowType.value = ActiveWindowListingType.SORTING
    }

    fun deleteSubscription(subId : Long) {
        viewModelScope.launch {
            val ops = getString(strings.operationSuccess)

            postOperationFields(
                subId,
                "delete_subscription",
                "subscriptions",
                onSuccess = {
                    val eventParameters = mapOf(
                        "buyer_id" to UserData.login,
                        "item_id" to subId
                    )
                    analyticsHelper.reportEvent(
                        "delete_subscription",
                        eventParameters
                    )
                    showToast(
                        successToastItem.copy(
                            message = ops
                        )
                    )
                    updateItem.value = subId
                },
                errorCallback = {

                }
            )

            deleteId.value = 1L
        }
    }


    fun applySorting(newSort: Sort?) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    sort = newSort
                )
            )
        }
        refresh()
        _activeWindowType.value = ActiveWindowListingType.LISTING
    }

    fun closeDialog(){
        deleteId.value = 1L
    }

    fun updateItem(sub : Subscription){
        viewModelScope.launch {
            getSubscription(sub.id){ item ->
                if (item != null) {
                    sub.catpath = item.catpath
                    sub.isEnabled = item.isEnabled
                    sub.name = item.name
                    sub.priceFrom = item.priceFrom
                    sub.priceTo = item.priceTo
                    sub.region = item.region
                    sub.searchQuery = item.searchQuery
                    sub.saleType = item.saleType
                } else {
                    sub.id = 1L
                }
                updateItem.value = null
            }
        }
    }
}

data class SubItemEventsImpl(
    val sub : Subscription,
    val component: SubscriptionsComponent,
    val viewModel: SubViewModel
) : SubItemEvents {
    override fun changeActiveSub() {
        if (sub.isEnabled)
            viewModel.disableSubscription(sub.id)
        else
            viewModel.enableSubscription(sub.id)
    }

    override fun getMenuOperations(callback : (List<MenuItem>) -> Unit) {
        viewModel.getSubOperations(sub.id) { listOperations ->
            callback(
                buildList {
                    addAll(listOperations.map { operation ->
                        MenuItem(
                            id = operation.id ?: "",
                            title = operation.name ?: "",
                            onClick = {
                                when (operation.id)  {
                                    "edit_subscription" ->{
                                        component.goToCreateNewSubscription(sub.id)
                                    }
                                    "delete_subscription" ->{
                                        viewModel.titleDialog.value = AnnotatedString(operation.name?:"")
                                        viewModel.deleteId.value = sub.id
                                    }
                                    else -> {
                                        viewModel.postOperationFields(
                                            sub.id,
                                            operation.id ?: "",
                                            "subscriptions",
                                            onSuccess = {
                                                val eventParameters = mapOf(
                                                    "buyer_id" to UserData.login,
                                                    "item_id" to sub.id
                                                )
                                                viewModel.analyticsHelper.reportEvent(
                                                    "delete_subscription",
                                                    eventParameters
                                                )

                                                viewModel.updateItem.value = sub.id
                                            },
                                            errorCallback = {

                                            }
                                        )
                                    }
                                }
                            }
                        )
                    })
                }
            )
        }
    }

    override fun onUpdateItem() {
       viewModel.updateItem(sub)
    }

    override fun onItemClick() {
        component.goToListing(sub)
    }
}



