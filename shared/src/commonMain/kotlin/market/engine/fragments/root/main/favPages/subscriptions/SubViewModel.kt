package market.engine.fragments.root.main.favPages.subscriptions

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.SubItemEvents
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.SubItemState
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.getMainTread
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class SubViewModel(component: SubscriptionsComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle)
{
    val subOperations : SubscriptionOperations by lazy { getKoin().get() }

    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    val listingBaseViewModel = component.additionalModels.value.listingBaseViewModel
    val listingData = listingBaseViewModel.listingData
    val activeWindowType = listingBaseViewModel.activeWindowType

    val deleteId = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "deleteId",
        1L,
        Long.serializer()
    )

    val titleDialog = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "titleDialog",
        "",
        String.serializer()
    )


    val pagingParamsFlow: Flow<ListingData> = combine(
        listingData,
        updatePage
    ) { listingData, _ ->

        listingBaseViewModel.setListItemsFilterBar(
            buildList {
                val createSbStr = getString(strings.createNewSubscriptionTitle)
                val sortString = getString(strings.sort)
                add(
                    NavigationItem(
                        title = createSbStr,
                    )
                )
                add(
                    NavigationItem(
                        title = sortString,
                        hasNews = listingData.data.sort != null,
                        badgeCount = null,
                    )
                )
            }
        )

        resetScroll()
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
        }.cachedIn(viewModelScope)

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
        }
    }

    fun getSubscription(subId : Long, onSuccess : (Subscription?) -> Unit ) {
         viewModelScope.launch {
             val buffer = withContext(Dispatchers.IO) {
                 subOperations.getSubscription(
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
                                when (operation.id) {
                                    "edit_subscription" -> {
                                        component.goToCreateNewSubscription(sub.id)
                                    }

                                    "delete_subscription" -> {
                                        viewModel.titleDialog.value = operation.name ?: ""
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
        viewModel.getMainTread {
            component.goToListing(sub)
        }
    }
}



