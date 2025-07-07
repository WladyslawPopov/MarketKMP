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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.SubItemEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.SubItemState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.ListingBaseViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class SubViewModel(component: SubscriptionsComponent) : CoreViewModel()
{
    private val subscriptionOperations: SubscriptionOperations = getKoin().get()

    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    private val _filterListingBtnItem = MutableStateFlow<List<FilterListingBtnItem>>(emptyList())
    val filterListingBtnItem: StateFlow<List<FilterListingBtnItem>> = _filterListingBtnItem.asStateFlow()

    val listingBaseViewModel = ListingBaseViewModel()
    val listingData = listingBaseViewModel.listingData
    val activeWindowType = listingBaseViewModel.activeWindowType

    val deleteId = MutableStateFlow(1L)
    val titleDialog = MutableStateFlow(AnnotatedString(""))

    val subOperations : SubscriptionOperations by lazy { getKoin().get() }

    val pagingParamsFlow: Flow<ListingData> = combine(
        listingData,
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
                listingBaseViewModel.setTotalCount(tc)
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

    init {
        viewModelScope.launch {
            listingBaseViewModel.setListingData(
                ListingData(
                    data = LD(
                        methodServer = "get_cabinet_listing",
                        objServer = "subscriptions",
                    )
                )
            )

            _filterListingBtnItem.value = listOf(
                FilterListingBtnItem(
                text = listingData.value.data.sort?.interpretation ?: "",
                removeFilter = {
                    listingBaseViewModel.setListingData(
                        listingData.value.copy(
                            data = listingData.value.data.copy(
                                sort = null
                            )
                        )
                    )
                    refresh()
                },
                itemClick = {
                    listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.SORTING)
                }
            ))

            listingBaseViewModel.setListItemsFilterBar(
                buildList {
                    val filterString = getString(strings.filter)
                    val sortString = getString(strings.sort)
                    val filters = listingData.value.data.filters.filter {
                        it.value != "" &&
                                it.interpretation?.isNotBlank() == true
                    }

                    add(
                        NavigationItem(
                            title = filterString,
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.FILTERS)
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = listingData.value.data.sort != null,
                            badgeCount = null,
                            onClick = {
                                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.SORTING)
                            }
                        )
                    )
                }
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
                    setUpdateItem(subId)
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
                    setUpdateItem(subId)
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
            activeWindowType.value != ActiveWindowListingType.LISTING ->{
                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
            }
            else -> {

            }
        }
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
                    setUpdateItem(subId)
                },
                errorCallback = {

                }
            )

            deleteId.value = 1L
        }
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
                setUpdateItem(null)
            }
        }
    }

    fun getSubOperations(subId : Long, onSuccess: (List<Operations>) -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) { subOperations.getOperationsSubscription(subId) }
            withContext(Dispatchers.Main) {
                val buf = res.success

                if (buf != null) {
                    onSuccess(buf)
                }
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

                                                viewModel.setUpdateItem(sub.id)
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



