package market.engine.fragments.root.main.offer

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.Platform
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.data.types.OfferStates
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.User
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import org.jetbrains.compose.resources.getString

data class OfferViewState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val offer: Offer = Offer(),

    val statusList: List<String> = emptyList(),
    val promoList: List<MenuItem> = emptyList(),
    val menuList: List<MenuItem> = emptyList(),
    val images: List<String> = emptyList(),

    val isMyOffer: Boolean = false,
    val offerState: OfferStates = OfferStates.ACTIVE,
    val remainingTime: Long = 1
)

class OfferViewModel(
    private val dataBase: MarketDB,
    private val component: OfferComponent,
    val offerId : Long = 1,
    val isSnapshot : Boolean = false
) : BaseViewModel() {

    private val _responseOffer: MutableStateFlow<Offer?> = MutableStateFlow(null)

    private val _responseHistory = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseHistory: StateFlow<List<OfferItem>> = _responseHistory.asStateFlow()
    private val _responseOurChoice = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseOurChoice: StateFlow<List<OfferItem>> = _responseOurChoice.asStateFlow()
    private val _responseCatHistory = MutableStateFlow<List<Category>>(emptyList())
    val responseCatHistory: StateFlow<List<Category>> = _responseCatHistory.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)

    private var timerJob: Job? = null
    private var timerBidsJob: Job? = null
    private var eventParameters: Map<String, Any?> = mapOf()

    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()

    val showOperationsDialog = MutableStateFlow("")
    val titleDialog = MutableStateFlow(AnnotatedString(""))
    val fieldsDialog = MutableStateFlow< ArrayList<Fields>>(arrayListOf())
    val dialogItemId = MutableStateFlow(1L)
    val myMaximalBid = MutableStateFlow("")

    private val _menuList = MutableStateFlow<List<MenuItem>>(emptyList())

    private val _operationsList = MutableStateFlow<List<MenuItem>>(emptyList())
    private val _promoList = MutableStateFlow<List<MenuItem>>(emptyList())

    val offerViewState : StateFlow<OfferViewState> = combine(
        _responseOffer,
        _remainingTime,
        _menuList,
        _operationsList,
        _promoList
    ) { offer, remainingTime, menuList, operationsList, promoList ->
        if (offer == null) return@combine OfferViewState(remainingTime = remainingTime)

        val images = when {
            offer.images?.isNotEmpty() == true -> offer.images?.map { it.urls?.big?.content.orEmpty() }
                ?: emptyList()

            offer.externalImages?.isNotEmpty() == true -> offer.externalImages
            else -> listOf("empty")
        }

        val copiedString = getString(strings.idCopied)

        //init timers
        val initTimer =
            ((offer.session?.end?.toLongOrNull()
                ?: 1L) - (getCurrentDate().toLongOrNull()
                ?: 1L)) * 1000

        eventParameters = mapOf(
            "lot_id" to offer.id,
            "lot_name" to offer.title,
            "lot_city" to offer.freeLocation,
            "auc_delivery" to offer.safeDeal,
            "lot_category_id" to offer.catpath.lastOrNull(),
            "seller_id" to offer.sellerData?.id,
            "lot_price_start" to offer.currentPricePerItem,
            "visitor_id" to UserData.userInfo?.id
        )

        val offerState = when {
            isSnapshot -> {
                analyticsHelper.reportEvent("view_item_snapshot", eventParameters)
                OfferStates.SNAPSHOT
            }

            offer.isPrototype -> {
                analyticsHelper.reportEvent("view_item_prototype", eventParameters)
                OfferStates.PROTOTYPE
            }

            offer.state == "active" -> {
                analyticsHelper.reportEvent("view_item", eventParameters)
                when {
                    (offer.session?.start?.toLongOrNull()
                        ?: 1L) > getCurrentDate().toLong() -> OfferStates.FUTURE

                    (offer.session?.end?.toLongOrNull()
                        ?: 1L) - getCurrentDate().toLong() > 0 -> OfferStates.ACTIVE

                    else -> OfferStates.COMPLETED
                }
            }

            offer.state == "sleeping" -> {
                analyticsHelper.reportEvent("view_item", eventParameters)
                if (offer.session == null || offer.buyerData != null) OfferStates.COMPLETED else OfferStates.INACTIVE
            }

            else -> {
                analyticsHelper.reportEvent("view_item", eventParameters)
                OfferStates.ACTIVE
            }
        }

        if (offer.saleType != "buy_now" && offerState == OfferStates.ACTIVE) {
            startTimerUpdateBids(offer)
        }

        if (initTimer < 24 * 60 * 60 * 1000 && offerState == OfferStates.ACTIVE) {
            startTimer(initTimer) {
                getOffer(offer.id)
            }
        } else {
            _remainingTime.value = initTimer
        }

        getCategoriesHistory(offer.catpath)
        updateUserInfo()

        myMaximalBid.value = offer.minimalAcceptablePrice ?: offer.currentPricePerItem ?: ""

        offer.sellerData = getUserInfo(offer.sellerData?.id ?: 1L) ?: offer.sellerData

        val defList = buildList {
            add(MenuItem(
                id = "copyId",
                title = getString(strings.copyOfferId),
                icon = drawables.copyIcon,
                onClick = {
                    clipBoardEvent(offer.id.toString())
                    showToast(
                        successToastItem.copy(
                            message = copiedString
                        )
                    )
                }
            ))

            add(MenuItem(
                id = "share",
                title = getString(strings.shareOffer),
                icon = drawables.shareIcon,
                onClick = {
                    offer.publicUrl?.let { openShare(it) }
                }
            ))

            add(MenuItem(
                id = "calendar",
                title = getString(strings.addToCalendar),
                icon = drawables.calendarIcon,
                onClick = {
                    offer.publicUrl?.let { openCalendarEvent(it) }
                }
            ))

            if (UserData.token != "") {
                add(
                    MenuItem(
                        id = "create_blank_offer_list",
                        title = getString(strings.createNewOffersListLabel),
                        icon = drawables.addFolderIcon,
                        onClick = {
                            getFieldsCreateBlankOfferList { t, f ->
                                titleDialog.value = AnnotatedString(t)
                                fieldsDialog.value.clear()
                                fieldsDialog.value.addAll(f)
                                showOperationsDialog.value = "create_blank_offer_list"
                            }
                        }
                    )

                )
            }
        }
        val listItems = listOf(
            NavigationItem(
                title = "",
                icon = drawables.recycleIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                badgeCount = null,
                onClick = {
                    refreshPage()
                }
            ),
            NavigationItem(
                title = getString(strings.editLabel),
                icon = drawables.editIcon,
                tint = colors.black,
                hasNews = false,
                badgeCount = null,
                isVisible = operationsList.find { it.id == "edit_offer" } != null,
                onClick = {
                    operationsList.find { it.id == "edit_offer" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = getString(strings.myNotesTitle),
                icon = drawables.editNoteIcon,
                tint = colors.black,
                hasNews = false,
                badgeCount = null,
                isVisible = operationsList.find { it.id == "create_note" || it.id == "edit_note" } != null,
                onClick = {
                    operationsList.find { it.id == "create_note" || it.id == "edit_note" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = getString(strings.favoritesTitle),
                icon = if (operationsList.find { it.id == "watch" } != null) drawables.favoritesIconSelected else drawables.favoritesIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                badgeCount = null,
                isVisible = operationsList.find { it.id == "watch" || it.id == "unwatch" } != null,
                onClick = {
                    operationsList.find { it.id == "watch" || it.id == "unwatch" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = getString(strings.menuTitle),
                icon = drawables.menuIcon,
                tint = colors.black,
                hasNews = false,
                badgeCount = null,
                onClick = {
                    _menuList.value = defList
                }
            )
        )

        offer.isProposalEnabled = operationsList.find { it.id == "make_proposal" } != null

        OfferViewState(
            appBarData = SimpleAppBarData(
                onBackClick = {
                    component.onBackClick()
                },
                listItems = listItems,
                menuItems = menuList,
                closeMenu = {
                    _menuList.value = emptyList()
                }
            ),
            offer = offer,
            statusList = checkStatusSeller(offer.sellerData?.id ?: 0),
            promoList = promoList,
            menuList = operationsList,
            images = images,
            isMyOffer = offer.sellerData?.login == UserData.userInfo?.login,
            offerState = offerState,
            remainingTime = remainingTime
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        OfferViewState()
    )

    init {
        refreshPage()
    }

    fun onAddBidClick(bid : String){
        if (UserData.token != "") {
            myMaximalBid.value = bid
            openDialog()
        } else {
            component.goToLogin()
        }
    }
    fun openDialog(){
        _showDialog.value = true
    }

    fun closeDialog(){
        _showDialog.value = false
    }

    fun updateUserState(id: Long){
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                getUserInfo(id)
            }
            withContext(Dispatchers.Main) {
                if (user != null) {
                    _responseOffer.update {
                        it?.copy(
                            sellerData = user
                        )
                    }
                }
            }
        }
    }

    fun refreshPage() {
        getOffer(offerId, isSnapshot)
        refresh()
    }

    fun editNote(id: Long){
        viewModelScope.launch {
            getOperationFields(
                id,
                "edit_note",
                "offers",
                onSuccess = { t, f ->
                    titleDialog.value = AnnotatedString(t)
                    fieldsDialog.value.clear()
                    fieldsDialog.value.addAll(f)
                    showOperationsDialog.value = "edit_note"
                }
            )
        }
    }

    fun deleteNote(id: Long){
        viewModelScope.launch {
            deleteNote(id) {
                refreshPage()
            }
        }
    }

    fun getOffer(offerId: Long, isSnapshot: Boolean = false) {
        viewModelScope.launch {
            try {
                setLoading(true)
                getHistory(offerId)
                getOurChoice(offerId)

                withContext(Dispatchers.IO) {
                    val response =
                        if (isSnapshot) apiService.getOfferSnapshots(offerId) else apiService.getOffer(offerId)
                    val serializer = ListSerializer(Offer.serializer())
                    val data = deserializePayload(response.payload, serializer).firstOrNull()
                    data?.let { offer ->
                        updateOperations(offer)
                        _responseOffer.value = offer
                    }
                }

            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Unknown error", ""))
            }
        }
    }

    fun updateOperations(offer: Offer){
        viewModelScope.launch {
            val currency = getString(strings.currencyCode)
            getOfferOperations(
                offer.id
            ) { list ->
                _operationsList.value = buildList {
                    addAll(list.map { operation ->
                        MenuItem(
                            id = operation.id ?: "",
                            title = operation.name ?: "",
                            onClick = {
                                operation.run {
                                    when {
                                        id == "activate_offer_for_future" || id == "activate_offer" -> {
                                            titleDialog.value = AnnotatedString(name ?: "")
                                            showOperationsDialog.value = id
                                            dialogItemId.value = offer.id
                                        }

                                        id == "copy_offer_without_old_photo" -> {
                                            component.goToCreateOffer(
                                                CreateOfferType.COPY_WITHOUT_IMAGE,
                                                offer.catpath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "edit_offer" -> {
                                            component.goToCreateOffer(
                                                CreateOfferType.EDIT,
                                                offer.catpath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "copy_offer" -> {
                                            component.goToCreateOffer(
                                                CreateOfferType.COPY,
                                                offer.catpath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "act_on_proposal" -> {
                                            component.goToProposalPage(
                                                ProposalType.ACT_ON_PROPOSAL
                                            )
                                        }

                                        id == "make_proposal" -> {
                                            component.goToProposalPage(
                                                ProposalType.MAKE_PROPOSAL
                                            )
                                        }

                                        id == "cancel_all_bids" -> {
                                            component.goToDynamicSettings(
                                                "cancel_all_bids",
                                                offer.id
                                            )
                                        }

                                        id == "remove_bids_of_users" -> {
                                            component.goToDynamicSettings(
                                                "remove_bids_of_users",
                                                offer.id
                                            )
                                        }

                                        isDataless == false -> {
                                            getOperationFields(
                                                offer.id,
                                                id ?: "",
                                                "offers",
                                            ) { t, f ->
                                                titleDialog.value = AnnotatedString(t)
                                                fieldsDialog.value.clear()
                                                fieldsDialog.value.addAll(f)
                                                showOperationsDialog.value = id ?: ""
                                                dialogItemId.value = offer.id
                                            }
                                        }

                                        else -> {
                                            postOperationFields(
                                                offer.id,
                                                id ?: "",
                                                "offers",
                                                onSuccess = {
                                                    val eventParameters = mapOf(
                                                        "lot_id" to offer.id,
                                                        "lot_name" to offer.title,
                                                        "lot_city" to offer.freeLocation,
                                                        "auc_delivery" to offer.safeDeal,
                                                        "lot_category" to offer.catpath.firstOrNull(),
                                                        "seller_id" to offer.sellerData?.id,
                                                        "lot_price_start" to offer.currentPricePerItem,
                                                    )
                                                    analyticsHelper.reportEvent(
                                                        "${id}_success",
                                                        eventParameters
                                                    )

                                                    updateUserInfo()
                                                    when (operation.id) {
                                                        "watch", "unwatch", "create_blank_offer_list" -> {
                                                            updateOperations(offer)
                                                        }

                                                        else -> {
                                                            refreshPage()
                                                        }
                                                    }
                                                },
                                                errorCallback = {}
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    })
                }
            }

            getOfferOperations(
                offer.id,
                "promo"
            ) { listOperations ->
                _promoList.value = buildList {
                    addAll(listOperations.map { operation ->
                        MenuItem(
                            id = operation.id ?: "",
                            title = "${(operation.name ?: "")} (${operation.price * -1}$currency)",
                            onClick = {
                                getOperationFields(
                                    offer.id,
                                    operation.id ?: "",
                                    "offers"
                                ) { t, f ->
                                    titleDialog.value = buildAnnotatedString {
                                        append(t)
                                        withStyle(
                                            SpanStyle(
                                                color = colors.notifyTextColor,
                                            )
                                        ) {
                                            append(" ${operation.price}$currency")
                                        }
                                    }
                                    fieldsDialog.value.clear()
                                    fieldsDialog.value.addAll(f)
                                    showOperationsDialog.value =
                                        operation.id ?: ""
                                    dialogItemId.value = offer.id
                                }
                            }
                        )
                    })
                }
            }
        }
    }

    fun clearDialogFields(){
        dialogItemId.value = 1
        fieldsDialog.value.clear()
        showOperationsDialog.value = ""
    }

    suspend fun getUserInfo(id: Long) : User? {
        return try {
            val res = withContext(Dispatchers.IO) {
                userOperations.getUsers(id)
            }

            withContext(Dispatchers.Main) {
                val user = res.success?.firstOrNull()
                val error = res.error
                if (user != null) {
                    return@withContext user
                } else {
                    error?.let { throw it }
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            null
        }
    }


    private fun getHistory(currentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _responseHistory.value = arrayListOf()
                val queries = dataBase.offerVisitedHistoryQueries
                val historyIds = queries.selectAll(UserData.login).executeAsList()

                // Delete the oldest entry if the history size exceeds the limit.
                if (historyIds.size >= 17) {
                    queries.deleteById(historyIds.last(), UserData.login)
                }
                // Insert the current offer ID into the history.
                queries.insertEntry(currentId, UserData.login)

                // Fetch offer details for each history ID and update the response history.
                _responseHistory.value = buildList {
                    historyIds.forEach { id ->
                        val response = apiService.getOffer(id)
                        val serializer = ListSerializer(Offer.serializer())
                        val offer = deserializePayload(response.payload, serializer).firstOrNull()
                        offer?.let {
                            // Update the response history only on the main thread.
                            withContext(Dispatchers.Main) {
                                add(it.parseToOfferItem())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching history", ""))
            }
        }
    }

    fun addHistory(id: Long) {
        val sh = dataBase.offerVisitedHistoryQueries
        sh.insertEntry(id, UserData.login)
    }

    private fun getOurChoice(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getOurChoiceOffers(id)
                val serializer = Payload.serializer(Offer.serializer())
                val ourChoice = deserializePayload(response.payload, serializer).objects
                _responseOurChoice.value = ourChoice.map { it.parseToOfferItem() }.toList()
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching our choice", ""))
            }
        }
    }

    private fun getCategoriesHistory(catPath: List<Long>) {
        viewModelScope.launch {
            try {
                val categories = withContext(Dispatchers.IO) {
                    catPath.reversed().mapNotNull { id ->
                        categoryOperations.getCategoryInfo(id).success
                    }
                }
                _responseCatHistory.value = categories
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching categories", ""))
            }
        }
    }

    private fun startTimer(initialTime: Long, onFinish: () -> Unit) {
        _remainingTime.value = initialTime
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0) {
                delay(1000L)
                _remainingTime.value -= 1000L
            }
            onFinish()
        }
    }

    private fun startTimerUpdateBids(offer: Offer) {
        val initialTime =
            (offer.session?.end?.toLongOrNull()?.let { it - getCurrentDate().toLong() }
                ?: 0L) * 1000

        timerBidsJob?.cancel()
        timerBidsJob = viewModelScope.launch {
            var millisUntilFinished = initialTime
            var currentInterval = calculateNewInterval(millisUntilFinished)

            while (millisUntilFinished > 0) {
                updateBidsInfo(offer)
                val newInterval = calculateNewInterval(millisUntilFinished)
                if (newInterval != currentInterval) {
                    currentInterval = newInterval
                }

                delay(currentInterval)
                millisUntilFinished -= currentInterval
            }
            timerBidsJob?.cancel()
        }
    }

    private fun calculateNewInterval(millisUntilFinished: Long): Long {
        return when {
            millisUntilFinished > 2 * 60 * 1000 -> 10_000L // Every 10 seconds
            millisUntilFinished in 1 * 60 * 1000..2 * 60 * 1000 -> 5_000L // Every 5 seconds
            millisUntilFinished in 10_000 + 1..1 * 60 * 1000 -> 2_000L // Every 2 seconds
            else -> 1_000L // Every second
        }
    }

    fun updateBidsInfo(offer: Offer) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    offerOperations.postGetLeaderAndPrice(offer.id, offer.version)
                }
                withContext(Dispatchers.Main) {
                    response.success?.body?.let { body ->
                        if (body.isChanged) {
                            _responseOffer.update {
                                it?.copy(
                                    bids = body.bids,
                                    version = JsonPrimitive(body.currentVersion),
                                    currentPricePerItem = body.currentPrice,
                                    minimalAcceptablePrice = body.minimalAcceptablePrice,
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error updating bids info", ""))
            }
        }
    }

    fun addBid(
        sum: String,
        offer: Offer,
        onSuccess: () -> Unit,
        onDismiss: () -> Unit
    ) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationAdditionalData(
                    offer.id,
                    "add_bid",
                    "offers",
                    hashMapOf("price" to JsonPrimitive(sum))
                )
            }

            val buf = res.success
            val error = res.error

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    showToast(
                        successToastItem.copy(
                            message = buf.operationResult?.result ?: getString(strings.operationSuccess)
                        )
                    )
                    val eventParameters = mapOf(
                        "lot_id" to offer.id,
                        "lot_name" to offer.name,
                        "lot_city" to offer.freeLocation,
                        "auc_delivery" to offer.safeDeal,
                        "lot_category" to offer.catpath.firstOrNull(),
                        "seller_id" to offer.sellerData?.id,
                        "lot_price_start" to offer.currentPricePerItem,
                        "buyer_id" to UserData.login,
                        "bid_amount" to sum,
                        "bids_all" to offer.bids?.size
                    )
                    analyticsHelper.reportEvent(
                        "bid_made",
                        eventParameters
                    )
                    onSuccess()
                    onDismiss()
                } else {
                    error?.let { onError(it) }
                    onDismiss()
                }
            }
        }
    }

    fun clearTimers() {
        timerJob?.cancel()
        timerBidsJob?.cancel()
    }
}
