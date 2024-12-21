package market.engine.fragments.base

import androidx.compose.material.BottomSheetValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import market.engine.core.network.ServerErrorException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.ToastItem
import market.engine.core.network.APIService
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.repositories.SettingsRepository
import market.engine.core.data.types.ToastType
import org.koin.mp.KoinPlatform.getKoin

open class BaseViewModel: ViewModel() {
    //select items and updateItem
    var selectItems : MutableList<Long> = mutableStateListOf()
    var updateItem : MutableState<Long?> = mutableStateOf(null)

    //filters params
    val isOpenSearch : MutableState<Boolean> = mutableStateOf(false) // first open search
    var activeFiltersType : MutableState<String> = mutableStateOf("")
    var bottomSheetState : MutableState<BottomSheetValue> = mutableStateOf(BottomSheetValue.Collapsed)

    private val apiService = getKoin().get<APIService>()
    private val offersOperations : OfferOperations = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    suspend fun getUpdatedOfferById(offerId: Long) : Offer? {
        return try {
            val response = offersOperations.getOffer(offerId)
            response.success?.let {
                return it
            }
        } catch (_: Exception) {
            null
        }
    }

    private val _errorMessage = MutableStateFlow(ServerErrorException())
    val errorMessage: StateFlow<ServerErrorException> = _errorMessage.asStateFlow()

    val toastItem = mutableStateOf(ToastItem(message = "", type = ToastType.WARNING, isVisible = false))

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    val viewModelScope = CoroutineScope(Dispatchers.Default)

    fun onError(exception: ServerErrorException) {
        _errorMessage.value = exception
    }

    fun setLoading(isLoading: Boolean) {
        _isShowProgress.value = isLoading
    }

    fun showToast(newToast: ToastItem) {
        toastItem.value = newToast
        viewModelScope.launch {
            delay(3000)
            toastItem.value = ToastItem(message = "", type = ToastType.WARNING, isVisible = false)
        }
    }

    val settings : SettingsRepository = getKoin().get()


    fun getCategories(searchData : SD, listingData : LD, withoutCounter : Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!searchData.searchIsLeaf) {
                    val id = searchData.searchCategoryID

                    val response = apiService.getPublicCategories(id)

                    val serializer = Payload.serializer(Category.serializer())
                    val payload: Payload<Category> =
                        deserializePayload(response.payload, serializer)

                    val categories = if (!withoutCounter) {
                        val categoriesWithLotCounts = payload.objects.map { category ->
                            async {
                                val sd = searchData.copy()
                                sd.searchCategoryID = category.id
                                val lotCount = categoryOperations.getTotalCount(
                                    sd, listingData
                                )
                                category.copy(estimatedActiveOffersCount = lotCount.success ?: 0)
                            }
                        }

                        categoriesWithLotCounts.awaitAll()
                            .filter { it.estimatedActiveOffersCount > 0 }
                    } else {
                        payload.objects.filter { it.estimatedActiveOffersCount > 0 }
                    }

                    _responseCategory.value = categories
                }else{
                    if(activeFiltersType.value == "categories") {
                        val category = onCatBack(searchData.searchParentID ?: 1L)
                        if (category != null) {
                            val sd = searchData.copy(
                                searchCategoryID = category.id,
                                searchCategoryName = category.name,
                                searchParentID = category.parentId,
                                searchIsLeaf = category.isLeaf
                            )
                            getCategories(sd, listingData, withoutCounter)
                        }
                    }
                }

            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            }
            finally {
                setLoading(false)
            }
        }
    }

    suspend fun onCatBack(
        uploadId: Long,
    ) : Category? {
        val response = withContext(Dispatchers.IO) {
            categoryOperations.getCategoryInfo(
                uploadId
            )
        }
        return response.success
    }
}
