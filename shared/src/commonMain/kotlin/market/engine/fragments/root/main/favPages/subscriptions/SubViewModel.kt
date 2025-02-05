package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import market.engine.core.data.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class SubViewModel(
    val apiService: APIService,
    private val subscriptionOperations: SubscriptionOperations
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Subscription> = PagingRepository()

    val listingData = mutableStateOf(ListingData())


    fun init(): Flow<PagingData<Subscription>> {
        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "subscriptions"

        return pagingRepository.getListing(listingData.value, apiService, Subscription.serializer()).cachedIn(viewModelScope)
    }

    fun refresh(){
        pagingRepository.refresh()
    }

    suspend fun getSubscription(subId : Long) : Subscription? {
        val buffer = withContext(Dispatchers.IO) {
            subscriptionOperations.getSubscription(
                subId
            )
        }
        val res = buffer.success
        return if (res != null) {
            withContext(Dispatchers.Main) {
                return@withContext res
            }
        } else {
            withContext(Dispatchers.Main) {
                return@withContext null
            }
        }
    }

    suspend fun enableSubscription(subId : Long) : Boolean {
        val buffer = withContext(Dispatchers.IO) {
            subscriptionOperations.postSubOperationsEnable(
                subId
            )
        }
        val res = buffer.success
        val resError = buffer.error
        return withContext(Dispatchers.Main) {
            if (res != null) {
                return@withContext true
            } else {
                if (resError != null) {
                    onError(resError)
                }
                return@withContext false
            }
        }
    }

    suspend fun disableSubscription(subId : Long) : Boolean {
        val buffer = withContext(Dispatchers.IO) {
            subscriptionOperations.postSubOperationsDisable(
                subId
            )
        }
        val res = buffer.success
        val resError = buffer.error
        return withContext(Dispatchers.Main) {
            if (res != null) {
                return@withContext true
            } else {
                if (resError != null) {
                    onError(resError)
                }
                return@withContext false
            }
        }
    }

    suspend fun deleteSubscription(subId : Long) : Boolean {
        val buf = withContext(Dispatchers.IO) {
            subscriptionOperations.postSubOperationsDelete(
                subId
            )
        }
        return withContext(Dispatchers.Main) {
            val res = buf.success
            return@withContext res != null
        }
    }
}
