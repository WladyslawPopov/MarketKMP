package market.engine.fragments.root.main.basket

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.Platform
import market.engine.core.data.constants.minExpandedElement
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.BasketEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.states.BasketGroupUiState
import market.engine.core.data.states.BasketUiState
import market.engine.core.data.states.MenuData
import market.engine.core.data.states.SelectedBasketList
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.User
import market.engine.core.network.networkObjects.UserBody
import market.engine.core.utils.deserializePayload
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.getValue


class BasketViewModel(component: BasketComponent): CoreViewModel() {

    private var responseGetUserCart = MutableStateFlow<List<Pair<User?, List<OfferItem?>>>>(emptyList())
    private var selectedOffers = MutableStateFlow<List<SelectedBasketList>>(emptyList())
    private var showExpanded = MutableStateFlow<List<Pair<Long, Int>>>(emptyList())

    private val basketsEvents = BasketEventsImpl(this, component)
    private val _isMenuVisibility = MutableStateFlow(false)
    private val _subtitle = MutableStateFlow("")
    private val _deleteIds = MutableStateFlow(emptyList<Long>())

    private val userOperations : UserOperations by lazy { getKoin().get() }

    val uiDataState: StateFlow<List<BasketGroupUiState>> = combine(
        responseGetUserCart,
        selectedOffers,
        showExpanded
    ) { userBasket, selected, expanded ->
        userBasket.mapNotNull { (user, offers) ->
            if (user == null) return@mapNotNull null

            val selectedForUser = selected.find { it.userId == user.id }?.selectedOffers ?: emptyList()
            val expandedCount = expanded.find { it.first == user.id }?.second ?: minExpandedElement
            val buyableOffersCount = offers.count { it?.safeDeal == true }

            BasketGroupUiState(
                user = user,
                offersInGroup = offers,
                selectedOffers = selectedForUser,
                showItemsCount = expandedCount,
                isAllSelected = selectedForUser.isNotEmpty() && selectedForUser.size == buyableOffersCount,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val uiState: StateFlow<BasketUiState> = combine(
        _subtitle,
        _deleteIds,
                _isMenuVisibility
    ) { subtitle, deleteIds, isMenuVisibility ->
        val menuString = getString(strings.menuTitle)
        val clearBasketString = getString(strings.actionClearBasket)

        BasketUiState(
            appBarData = SimpleAppBarData(
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        onClick = {
                            refresh()
                        }
                    ),
                    NavigationItem(
                        title = menuString,
                        icon = drawables.menuIcon,
                        tint = colors.black,
                        hasNews = false,
                        badgeCount = null,
                        onClick = {
                           _isMenuVisibility.value = true
                        }
                    ),
                ),
                menuData = MenuData(
                    menuItems = listOf(
                        MenuItem(
                            id = "delete_basket",
                            title = clearBasketString,
                            icon = drawables.deleteIcon,
                            onClick = {
                                clearBasket{
                                    refresh()
                                }
                            }
                        )
                    ),
                    isMenuVisible = isMenuVisibility,
                    closeMenu = {
                       _isMenuVisibility.value = false
                    }
                ),
            ),
            basketEvents = basketsEvents,
            subtitle = subtitle,
            deleteIds = deleteIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = BasketUiState(
            basketEvents = basketsEvents
        )
    )

    init {
        viewModelScope.launch {
            val oneOffer = getString(strings.oneOfferLabel)
            val manyOffers = getString(strings.manyOffersLabel)
            val exManyOffers = getString(strings.exManyOffersLabel)

            snapshotFlow {
                UserData.userInfo
            }.collectLatest { info ->
                val countOffers = info?.countOffersInCart

                _subtitle.value = buildString {
                    if (countOffers.toString()
                            .matches(Regex("""([^1]1)$""")) || countOffers == 1
                    ) {
                        append("$countOffers $oneOffer")
                    } else if (countOffers.toString()
                            .matches(Regex("""([^1][234])$""")) || countOffers == 2 || countOffers == 3 || countOffers == 4
                    ) {
                        append("$countOffers $exManyOffers")
                    } else {
                        append("$countOffers $manyOffers")
                    }
                }
            }
        }
    }

    fun updateQuantityInState(offerId: Long, newQuantity: Int) {
        selectedOffers.value = selectedOffers.value.map { listForUser ->
            listForUser.copy(selectedOffers = listForUser.selectedOffers.map {
                if (it.offerId == offerId) it.copy(selectedQuantity = newQuantity) else it
            })
        }
    }

    fun getUserCart(){
        viewModelScope.launch {
            try {
                setLoading(true)
                updateUserInfo()
                val response = withContext(Dispatchers.IO) {
                    apiService.postOperation(UserData.login, "get_cart_items", "users", emptyMap())
                }

                try {
                    val serializer = BodyListPayload.serializer(UserBody.serializer())
                    val payload : BodyListPayload<UserBody> = deserializePayload(response.payload, serializer)

                    val groupedBySeller = payload.bodyList.groupBy { it.sellerId }
                        val result = groupedBySeller.map { (sellerId, items) ->
                            val sellerUser: User? = getUser(sellerId)

                            selectedOffers.value += SelectedBasketList(sellerId, listOf())
                            showExpanded.value += Pair(sellerId, minExpandedElement)

                            val basketItems: List<OfferItem?> = items.map { item ->
                                OfferItem(
                                    id = item.offerId,
                                    title = item.offerTitle ?: "",
                                    price = item.offerPrice ?: "",
                                    currentQuantity = item.availableQuantity,
                                    quantity = item.quantity,
                                    seller = User(id = item.sellerId),
                                    location = item.freeLocation ?: "",
                                    images = listOf(item.offerImage ?: ""),
                                    safeDeal = item.isBuyable == true,
                                    isWatchedByMe = item.isWatchedByMe == true,
                                    state = "active",
                                )
                            }
                            sellerUser to basketItems
                        }
                    responseGetUserCart.value = result
                }catch (_ : Exception){
                    throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(
                    ServerErrorException(
                        errorCode = exception.message.toString(),
                        humanMessage = exception.message.toString()
                    )
                )
            } finally {
                setLoading(false)
            }
        }
    }

    fun clickExpanded(userId: Long, currentOffersSize: Int) {
        val exItem = showExpanded.value.find { it.first == userId }

        val newList = showExpanded.value.toMutableList()
        exItem?.let {
            newList.remove(it)
            val newItem = if (exItem.second == minExpandedElement) {
                Pair(userId, currentOffersSize)
            } else {
                Pair(userId, minExpandedElement)
            }
            newList.add(newItem)
            showExpanded.value = newList
        }
    }

    fun checkSelected(userId: Long, item : SelectedBasketItem, checked: Boolean) {
        selectedOffers.value = selectedOffers.value.map { listForUser ->
            if (listForUser.userId == userId) {
                val updatedOffers = if (checked) {
                    listForUser.selectedOffers.filterNot { it.offerId == item.offerId  } + item
                } else {
                    listForUser.selectedOffers.filterNot { it.offerId == item.offerId }
                }

                listForUser.copy(selectedOffers = updatedOffers)
            }else{
                listForUser
            }
        }
    }

    fun uncheckAll(userId: Long){
        selectedOffers.value = selectedOffers.value.map { listForUser ->
            if (listForUser.userId == userId) {
                listForUser.copy(selectedOffers = emptyList())
            } else {
                listForUser
            }
        }
    }

    private suspend fun getUser(id : Long) : User? {
        try {
            val res = withContext(Dispatchers.IO){
                userOperations.getUsers(id)
            }

            return withContext(Dispatchers.Main){
                val user = res.success?.firstOrNull()
                val error = res.error
                if (user != null){
                    return@withContext user
                }else{
                    error?.let { throw it }
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            return null
        } catch (exception: Exception) {
            onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            return null
        }
    }

    fun clearBasket(onSuccess : () -> Unit) {
        if (UserData.token != "") {
            viewModelScope.launch {
                val resObj = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        UserData.login,
                        "delete_cart",
                        "users"
                    )
                }

                val res = resObj.success
                val resErr = resObj.error

                if (res != null) {
                    updateUserInfo()
                    showToast(
                        successToastItem.copy(message = getString(strings.operationSuccess))
                    )
                    onSuccess()
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun clearDeleteIds() {
        _deleteIds.value = emptyList()
    }

    fun setDeleteItems(list: List<Long>){
        _deleteIds.value = list
    }

    fun refreshPage(){
        getUserCart()
        refresh()
    }

    fun deleteItems(
        deleteIds: List<Long>,
        onSuccess: () -> Unit
    ) {
        val offerIds = arrayListOf<JsonPrimitive>()

        deleteIds.forEach {
            offerIds.add(JsonPrimitive(it))
        }

        val jsonArray = JsonArray(offerIds)

        val body = hashMapOf<String, JsonElement>()
        body["offer_ids"] = jsonArray

        val curItem = responseGetUserCart.value.find { pair ->
            pair.second.find { it?.id == deleteIds.firstOrNull() } != null
        }

        val userId = curItem?.first?.id ?: 1L
        val lotData = curItem?.second?.find { it?.id == deleteIds.firstOrNull() }

        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "remove_many_items_from_cart",
                    "users",
                    body
                )
            }
            val buffer = res.success
            val error = res.error

            withContext(Dispatchers.Main) {
                if (buffer != null) {
                    updateUserInfo()

                    val eventParameters = mapOf(
                        "lot_id" to lotData?.id,
                        "lot_name" to lotData?.title,
                        "lot_city" to lotData?.location,
                        "auc_delivery" to "false",
                        "lot_category" to "-",
                        "seller_id" to userId,
                        "lot_price_start" to lotData?.price,
                        "cart_id" to userId
                    )
                    analyticsHelper.reportEvent(
                        "click_del_item",
                        eventParameters
                    )
                    showToast(
                        successToastItem.copy(message = getString(strings.operationSuccess))
                    )
                    delay(1000)
                    uncheckAll(userId)
                    onSuccess()
                } else {
                    if (error != null) {
                        onError(error)
                    }
                }
            }
        }
    }

    fun addOfferToBasket(body : HashMap<String, JsonElement>, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "add_item_to_cart",
                    "users",
                    body
                )
            }

            val buffer = res.success
            val error = res.error

            if (buffer != null) {
                updateUserInfo()
                onSuccess(buffer.operationResult?.message ?: getString(strings.operationSuccess))
            } else {
                if (error != null) {
                    onError(error)
                }
            }
        }
    }
}

data class BasketEventsImpl(
    val viewModel: BasketViewModel,
    val component: BasketComponent
) : BasketEvents {
    override fun onOfferSelected(userId: Long, item: SelectedBasketItem, isChecked: Boolean) {
        viewModel.checkSelected(userId, item, isChecked)
    }
    override fun onExpandClicked(userId: Long, currentOffersSize: Int) {
        viewModel.clickExpanded(userId, currentOffersSize)
    }
    override fun onSelectAll(userId: Long, allOffers: List<OfferItem?>, isChecked: Boolean) {
        if (isChecked) {
            allOffers.filter { it?.safeDeal == true }.mapNotNull { it }.forEach { offer ->
                viewModel.checkSelected(userId, SelectedBasketItem(
                    offerId = offer.id,
                    pricePerItem = offer.price.toDouble(),
                    selectedQuantity = offer.quantity
                ), true)
            }
        } else {
            viewModel.uncheckAll(userId)
        }
    }
    override fun onQuantityChanged(offerId: Long, newQuantity: Int, onResult: (Int) -> Unit) {
        val body = HashMap<String, JsonElement>()
        body["offer_id"] = JsonPrimitive(offerId)
        body["quantity"] = JsonPrimitive(newQuantity)
        viewModel.addOfferToBasket(body) { onResult(newQuantity) }
        viewModel.updateQuantityInState(offerId, newQuantity)
    }
    override fun onAddToFavorites(offer: OfferItem, onFinish: (Boolean) -> Unit) {
        viewModel.addToFavorites(offer) {
            offer.isWatchedByMe = it
            onFinish(it)
        }
    }
    override fun onDeleteOffersRequest(ids: List<Long>) {
        viewModel.setDeleteItems(ids)
    }
    override fun onCreateOrder(userId: Long, selectedOffers: List<SelectedBasketItem>) {
        component.goToCreateOrder(Pair(userId, selectedOffers))
    }
    override fun onGoToUser(userId: Long) {
        component.goToUser(userId)
    }
    override fun onGoToOffer(offerId: Long) {
        component.goToOffer(offerId)
    }
}
