package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class SubViewModel(
    private val subscriptionOperations: SubscriptionOperations
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Subscription>> {
        listingData.value.data.methodServer = "get_cabinet_listing"
        listingData.value.data.objServer = "subscriptions"

        return pagingRepository.getListing(listingData.value, apiService, Subscription.serializer()).cachedIn(viewModelScope)
    }

    fun refresh(){
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
}
