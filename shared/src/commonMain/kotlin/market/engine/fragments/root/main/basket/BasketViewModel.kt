package market.engine.fragments.root.main.basket

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
import market.engine.core.data.items.MenuData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.states.BasketGroupUiState
import market.engine.core.data.states.BasketUiState
import market.engine.core.data.states.SelectedBasketList
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.states.BasketItem
import market.engine.core.data.states.ShowBasketItem
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.User
import market.engine.core.network.networkObjects.UserBody
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.getValue


class BasketViewModel(component: BasketComponent, savedStateHandle: SavedStateHandle): CoreViewModel(savedStateHandle) {

    private val responseGetUserCart = savedStateHandle.getSavedStateFlow(
        scope,
        "responseGetUserCart",
        emptyList(),
        ListSerializer(BasketItem.serializer())
    )

    private val selectedOffers = savedStateHandle.getSavedStateFlow(
        scope,
        "selectedOffers",
        emptyList(),
        ListSerializer(SelectedBasketList.serializer())
    )
    private val showExpanded = savedStateHandle.getSavedStateFlow(
        scope,
        "showExpanded",
        emptyList(),
        ListSerializer(ShowBasketItem.serializer())
    )

    private val basketsEvents = BasketEventsImpl(this, component)

    private val _isMenuVisibility = MutableStateFlow(false)

    private val _subtitle = savedStateHandle.getSavedStateFlow(
        scope,
        "subtitle",
        "",
        String.serializer()
    )
    private val _deleteIds = savedStateHandle.getSavedStateFlow(
        scope,
        "deleteIds",
        emptyList(),
        ListSerializer(Long.serializer())
    )

    private val userOperations : UserOperations by lazy { getKoin().get() }

    val uiDataState: StateFlow<List<BasketGroupUiState>> = combine(
        responseGetUserCart.state,
        selectedOffers.state,
        showExpanded.state
    ) { userBasket, selected, expanded ->
        userBasket.map { (user, offers) ->

            val selectedForUser = selected.find { it.userId == user.id }?.selectedOffers ?: emptyList()
            val expandedCount = expanded.find { it.userId == user.id }?.index ?: minExpandedElement
            val buyableOffersCount = offers.count { it.safeDeal }

            BasketGroupUiState(
                user = user,
                offersInGroup = offers,
                selectedOffers = selectedForUser,
                showItemsCount = expandedCount,
                isAllSelected = selectedForUser.isNotEmpty() && selectedForUser.size == buyableOffersCount,
            )
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val uiState: StateFlow<BasketUiState> = combine(
        _subtitle.state,
        _deleteIds.state,
        _isMenuVisibility
    ) { subtitle, deleteIds, isMenuVisibility ->
        val menuString = getString(strings.menuTitle)
        val clearBasketString = getString(strings.actionClearBasket)

        BasketUiState(
            appBarData = SimpleAppBarData(
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        onClick = {
                            refresh()
                        }
                    ),
                    NavigationItem(
                        title = menuString,
                        hasNews = false,
                        badgeCount = null,
                        icon = drawables.menuIcon,
                        tint = colors.black,
                        onClick = {
                            _isMenuVisibility.value = true
                        }
                    )
                ),
                menuData = MenuData(
                    menuItems = listOf(
                        MenuItem(
                            id = "delete_basket",
                            title = clearBasketString,
                            icon = drawables.deleteIcon,
                            onClick = {
                                clearBasket()
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
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = BasketUiState(
            basketEvents = basketsEvents
        )
    )

    init {
        scope.launch {
            val oneOffer = getString(strings.oneOfferLabel)
            val manyOffers = getString(strings.manyOffersLabel)
            val exManyOffers = getString(strings.exManyOffersLabel)
            withContext(Dispatchers.IO) {
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
    }

    fun updateQuantityInState(offerId: Long, newQuantity: Int) {
        selectedOffers.value = selectedOffers.value.map { listForUser ->
            listForUser.copy(selectedOffers = listForUser.selectedOffers.map {
                if (it.offerId == offerId) it.copy(selectedQuantity = newQuantity) else it
            })
        }
    }

    fun getUserCart(){
        scope.launch {
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
                    val basketItem = groupedBySeller.map { (sellerId, items) ->
                        val sellerUser: User? = getUser(sellerId)

                        selectedOffers.value += SelectedBasketList(sellerId, listOf())
                        showExpanded.value += ShowBasketItem(sellerId, minExpandedElement)

                        val basketItems: List<OfferItem> = items.map { item ->
                            OfferItem(
                                id = item.offerId,
                                title = item.offerTitle ?: "",
                                images = listOf(item.offerImage ?: ""),
                                isWatchedByMe = item.isWatchedByMe == true,
                                quantity = item.quantity,
                                currentQuantity = item.availableQuantity,
                                price = item.offerPrice ?: "",
                                seller = User(id = item.sellerId),
                                location = item.freeLocation ?: "",
                                safeDeal = item.isBuyable == true,
                                state = "active",
                            )
                        }
                        BasketItem(sellerUser ?: User(), basketItems)
                    }
                    responseGetUserCart.value = basketItem
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
        val exItem = showExpanded.value.find { it.userId == userId }

        val newList = showExpanded.value.toMutableList()
        exItem?.let {
            newList.remove(it)
            val newItem = if (exItem.index == minExpandedElement) {
                ShowBasketItem(userId, currentOffersSize)
            } else {
                ShowBasketItem(userId, minExpandedElement)
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

    fun clearBasket() {
        if (UserData.token != "") {
            scope.launch {
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
                    refreshPage()
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
        deleteIds: List<Long>
    ) {
        val offerIds = arrayListOf<JsonPrimitive>()

        deleteIds.forEach {
            offerIds.add(JsonPrimitive(it))
        }

        val jsonArray = JsonArray(offerIds)

        val body = hashMapOf<String, JsonElement>()
        body["offer_ids"] = jsonArray

        val curItem = responseGetUserCart.value.find { item ->
            item.offerList.find { it.id == deleteIds.firstOrNull() } != null
        }

        val userId = curItem?.user?.id ?: 1L
        val lotData = curItem?.offerList?.find { it.id == deleteIds.firstOrNull() }

        scope.launch {
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
                    uncheckAll(userId)
                    clearDeleteIds()
                    refreshPage()
                } else {
                    if (error != null) {
                        onError(error)
                    }
                }
            }
        }
    }

    fun addOfferToBasket(body : HashMap<String, JsonElement>, newQuantity : Int, offerId: Long) {
        scope.launch {
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
                showToast(
                    successToastItem.copy(message = getString(strings.operationSuccess))
                )
                updateQuantityInState(offerId, newQuantity)

            } else {
                if (error != null) {
                    onError(error)
                }
            }
        }
    }

    fun addToFavorites(offer : OfferItem)
    {
        if(UserData.token != "") {
            scope.launch {
                val buf = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        offer.id,
                        if (offer.isWatchedByMe) "unwatch" else "watch",
                        "offers"
                    )
                }

                val res = buf.success
                withContext(Dispatchers.Main) {
                    if (res != null && res.operationResult?.result == "ok") {
                        val eventParameters = mapOf(
                            "lot_id" to offer.id,
                            "lot_name" to offer.title,
                            "lot_city" to offer.location,
                            "auc_delivery" to offer.safeDeal,
                            "lot_category" to offer.catPath.firstOrNull(),
                            "seller_id" to offer.seller.id,
                            "lot_price_start" to offer.price,
                        )
                        if (!offer.isWatchedByMe) {
                            analyticsHelper.reportEvent("offer_watch", eventParameters)
                        } else {
                            analyticsHelper.reportEvent("offer_unwatch", eventParameters)
                        }

                        updateUserInfo()

                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        responseGetUserCart.update { userCart ->
                            userCart.map { item ->
                                item.copy(
                                    offerList = item.offerList.map {
                                        if (it.id == offer.id) {
                                            it.copy(isWatchedByMe = !it.isWatchedByMe)
                                        } else {
                                            it
                                        }
                                    }
                                )
                            }
                        }

                    } else {
                        if (buf.error != null)
                            onError(buf.error!!)
                    }
                }
            }
        }else{
            goToLogin()
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
    override fun onQuantityChanged(offerId: Long, newQuantity: Int) {
        val body = HashMap<String, JsonElement>()
        body["offer_id"] = JsonPrimitive(offerId)
        body["quantity"] = JsonPrimitive(newQuantity)
        viewModel.addOfferToBasket(body, newQuantity, offerId)
    }
    override fun onAddToFavorites(offer: OfferItem) {
        viewModel.addToFavorites(offer)
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
