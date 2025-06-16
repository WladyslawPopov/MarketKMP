package market.engine.fragments.root.main.favPages.subscriptions

import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.bars.appBars.SimpleAppBarData

data class SubContentState(
    val listingBaseState : ListingBaseState = ListingBaseState(),
    val appState : SimpleAppBarData = SimpleAppBarData(),
    val listingData : ListingData = ListingData(),
    val activeFilterListingBtnItem: FilterListingBtnItem = FilterListingBtnItem()
)

class SubViewModel(
    private val subscriptionOperations: SubscriptionOperations
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData().copy(
        data = LD(
            methodServer = "get_cabinet_listing",
            objServer = "subscriptions",
        )
    ))

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<Subscription>> = _listingData
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(listingParams, apiService, Subscription.serializer())
        }.cachedIn(viewModelScope)

    val subContentState: StateFlow<SubContentState> = combine(
        _listingData,
        activeWindowType
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
                    activeWindowType.value = ActiveWindowListingType.SORTING
                }
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SubContentState()
    )

    fun refresh(){
        _listingData.update {
            it.copy()
        }
        updateUserInfo()
        pagingRepository.refresh()
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

    fun enableSubscription(subId : Long, onSuccess : () -> Unit) {
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
                    onSuccess()
                } else {
                    if (resError != null) {
                        onError(resError)
                    }
                }
            }
        }
    }

    fun disableSubscription(subId : Long, onSuccess: () -> Unit) {
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
                    onSuccess()
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
                activeWindowType.value = ActiveWindowListingType.LISTING
            }
            else -> {

            }
        }
    }

    fun openSort(){
        activeWindowType.value = ActiveWindowListingType.SORTING
    }
}
