package market.engine.core.repositories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import market.engine.common.Platform
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.UrlBuilder
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.dialogs.CustomDialogState
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform.getKoin
import kotlin.collections.contains
import kotlin.toString

class CabinetOfferRepository(
    offer: Offer = Offer(),
    val listingData: ListingData = ListingData(),
    val events: OfferRepositoryEvents,
    val core: CoreViewModel
) {
    val offerOperations : OfferOperations by lazy { getKoin().get() }

    private val _offerState = MutableStateFlow(offer.parseToOfferItem())
    val offerState: StateFlow<OfferItem> = _offerState.asStateFlow()

    private val _operationsList = MutableStateFlow<List<Operations>>(emptyList())
    private val _promoOperationsList = MutableStateFlow<List<Operations>>(emptyList())

    private val _customDialogState = MutableStateFlow(CustomDialogState())
    val customDialogState = _customDialogState.asStateFlow()

    private val _myMaximalBid = MutableStateFlow(offer.myMaximalBid)
    val myMaximalBid = _myMaximalBid.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText = _messageText.asStateFlow()

    private val _valuesPickerState = MutableStateFlow("")
    val valuesPickerState = _valuesPickerState.asStateFlow()

    private val _futureTimeInSeconds = MutableStateFlow("")
    val futureTimeInSeconds = _futureTimeInSeconds.asStateFlow()

    private val _isMenuVisible = MutableStateFlow(false)
    val isMenuVisible = _isMenuVisible.asStateFlow()

    private val _isProposalEnabled = MutableStateFlow(false)
    val isProposalEnabled = _isProposalEnabled.asStateFlow()
    
    val annotatedTitle = mutableStateOf<AnnotatedString?>(null)

    val menuList = _operationsList.map { operations ->
        operations.map { operation ->
            MenuItem(
                id = operation.id ?: "",
                title = operation.name ?: "",
                onClick = {
                    operation.run {
                        when {
                            id == "copy_offer_without_old_photo" -> {
                                events.goToCreateOffer(
                                    CreateOfferType.COPY_WITHOUT_IMAGE,
                                    offerState.value.catPath,
                                    offerState.value.id,
                                    offerState.value.externalImages
                                )
                            }

                            id == "edit_offer" -> {
                                events.goToCreateOffer(
                                    CreateOfferType.EDIT,
                                    offerState.value.catPath,
                                    offerState.value.id,
                                    offerState.value.externalImages
                                )
                            }

                            id == "copy_offer" -> {
                                events.goToCreateOffer(
                                    CreateOfferType.COPY,
                                    offerState.value.catPath,
                                    offerState.value.id,
                                    offerState.value.externalImages
                                )
                            }

                            id == "act_on_proposal" -> {
                                events.goToProposalPage(
                                    offerState.value.id, ProposalType.ACT_ON_PROPOSAL
                                )
                            }

                            id == "make_proposal" -> {
                                events.goToProposalPage(
                                    offerState.value.id, ProposalType.MAKE_PROPOSAL
                                )
                            }

                            id == "cancel_all_bids" -> {
                                events.goToDynamicSettings(
                                    "cancel_all_bids",
                                    offerState.value.id
                                )
                            }

                            id == "remove_bids_of_users" -> {
                                events.goToDynamicSettings(
                                    "remove_bids_of_users",
                                    offerState.value.id
                                )
                            }

                            !isDataless -> {
                                core.getOperationFields(
                                    offerState.value.id,
                                    id ?: "",
                                    "offers",
                                )
                                { t, f ->
                                    var fields = f
                                    when (id) {
                                        "edit_offer_in_list", "add_to_list", "remove_from_list" -> {
                                            getOffersList { list ->
                                                when (id) {
                                                    "add_to_list" -> {
                                                        fields.firstOrNull()?.choices =
                                                            buildList {
                                                                list.filter {
                                                                    !it.offers.contains(
                                                                        offerState.value.id
                                                                    )
                                                                }.fastForEach { item ->
                                                                    add(
                                                                        Choices(
                                                                            code = JsonPrimitive(
                                                                                item.id
                                                                            ),
                                                                            name = item.title
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                    }

                                                    "remove_from_list" -> {
                                                        fields.firstOrNull()?.choices =
                                                            buildList {
                                                                list.filter {
                                                                    it.offers.contains(
                                                                        offerState.value.id
                                                                    )
                                                                }.fastForEach { item ->
                                                                    add(
                                                                        Choices(
                                                                            code = JsonPrimitive(
                                                                                item.id
                                                                            ),
                                                                            name = item.title
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                    }

                                                    "edit_offer_in_list" -> {
                                                        val newField = Fields(
                                                            widgetType = "checkbox_group",
                                                            choices = list.map {
                                                                Choices(
                                                                    code = JsonPrimitive(
                                                                        it.id
                                                                    ),
                                                                    name = it.title
                                                                )
                                                            },
                                                            data = fields.firstOrNull()?.data,
                                                            key = fields.firstOrNull()?.key,
                                                            errors = fields.firstOrNull()?.errors,
                                                            shortDescription = fields.firstOrNull()?.shortDescription,
                                                            longDescription = fields.firstOrNull()?.longDescription,
                                                            validators = fields.firstOrNull()?.validators,
                                                        )
                                                        fields.fastForEach {
                                                            if (it.widgetType == "input") {
                                                                it.widgetType = "hidden"
                                                            }
                                                        }
                                                        fields = buildList {
                                                            addAll(fields)
                                                            remove(newField)
                                                            add(newField)
                                                        }
                                                    }
                                                }

                                                _customDialogState.value =
                                                    CustomDialogState(
                                                        title = t,
                                                        fields = fields,
                                                        typeDialog = id,
                                                    )
                                            }
                                        }

                                        else -> {
                                            _customDialogState.value =
                                                CustomDialogState(
                                                    title = t,
                                                    fields = fields,
                                                    typeDialog = id ?: ""
                                                )
                                        }
                                    }
                                }
                            }

                            else -> {
                                core.postOperationFields(
                                    offerState.value.id,
                                    id ?: "",
                                    "offers",
                                    onSuccess = {
                                        val eventParameters = mapOf(
                                            "lot_id" to offerState.value.id,
                                            "lot_name" to offerState.value.title,
                                            "lot_city" to offerState.value.location,
                                            "auc_delivery" to offerState.value.safeDeal,
                                            "lot_category" to offerState.value.catPath.firstOrNull(),
                                            "seller_id" to offerState.value.seller.id,
                                            "lot_price_start" to offerState.value.price,
                                        )
                                        core.analyticsHelper.reportEvent(
                                            "${id}_success",
                                            eventParameters
                                        )
                                        when (operation.id) {
                                            "watch", "unwatch", "create_blank_offer_list" -> {}
                                            else -> {
                                                refreshOffer()
                                            }
                                        }
                                        updateItem()
                                        core.updateUserInfo()
                                    },
                                    errorCallback = {}
                                )
                            }
                        }
                    }
                }
            )
        }
    }.stateIn(
        core.viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val promoList = _promoOperationsList.map {
        val currency = getString(strings.currencyCode)
        it.map { operation ->
            MenuItem(
                id = operation.id ?: "",
                title = "${(operation.name ?: "")} (${operation.price * -1}$currency)",
                onClick = {
                    core.getOperationFields(
                        offerState.value.id,
                        operation.id ?: "",
                        "offers"
                    )
                    { t, f ->
                        annotatedTitle.value = buildAnnotatedString {
                            append(t)
                            withStyle(
                                SpanStyle(
                                    color = colors.notifyTextColor,
                                )
                            ) {
                                append(" ${operation.price}$currency")
                            }
                        }

                        _customDialogState.value = CustomDialogState(
                            title = "",
                            fields = f,
                            typeDialog = operation.id ?: "",
                        )
                    }
                }
            )
        }
    }.stateIn(
        core.viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    init {
        core.viewModelScope.launch {
            _offerState.collectLatest {
                updateOperations()
            }
        }
    }

    fun refreshOffer(){
        updateItem()
        events.refreshPage()
    }

    fun setNewOfferData(offer: OfferItem){
        _offerState.value = offer
    }

    private suspend fun getItem(): Offer? {
        return try {
            val filters = listingData.data.filters.map{
                if(it.key == "id"){
                    it.copy(
                        value = offerState.value.id.toString(),
                        interpretation = ""
                    )
                }else{
                    it.copy()
                }
            }
            val url = UrlBuilder()
                .addPathSegment(listingData.data.objServer)
                .addPathSegment(listingData.data.methodServer)
                .addFilters(listingData.data.copy(
                    filters = filters
                ), listingData.searchData)
                .build()

            val res = withContext(Dispatchers.IO) {
                core.apiService.getPage(url)
            }
            return withContext(Dispatchers.Main) {
                if (res.success) {
                    val serializer = Payload.serializer(Offer.serializer())
                    val payload = deserializePayload(res.payload, serializer)
                    return@withContext payload.objects.firstOrNull()
                }else{
                    return@withContext null
                }
            }
        } catch (exception: ServerErrorException) {
            core.onError(exception)
            null
        } catch (exception: Exception) {
            core.onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            null
        }
    }

    fun updateItem() {
        updateOperations()
        if(listingData.data.methodServer.isNotBlank()) {
            core.viewModelScope.launch {
                val buf = withContext(Dispatchers.IO) {
                    getItem()
                }

                withContext(Dispatchers.Main) {
                    if (buf != null) {
                        _offerState.update {
                            it.copy().setNewParams(buf)
                        }
                    } else {
                        _offerState.update {
                            it.copy(
                                session = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateOperations(){
        core.viewModelScope.launch {
            getOfferOperations(
                offerState.value.id
            ) { list ->
                _operationsList.value = list
                _isProposalEnabled.value = isProposalsEnabled()
            }

            getOfferOperations(
                offerState.value.id,
                "promo"
            ) { listOperations ->
                _promoOperationsList.value = listOperations
            }
        }
    }

    fun makeOperations(){
        var method = "offers"
        var idMethod =
            offerState.value.id

        val body = HashMap<String, JsonElement>()
        val fields = customDialogState.value.fields
        val id = customDialogState.value.typeDialog

        when(id){
            "send_message" -> {
                writeToSeller(
                    offerState.value.id, messageText.value,
                ) {
                    events.goToDialog(it)
                    clearDialogFields()
                }
            }
            "add_bid" -> {
                addBid(
                    myMaximalBid.value,
                    offerState.value,
                    onSuccess = {
                        events.updateBidsInfo(offerState.value)
                        events.scrollToBids()
                        clearDialogFields()
                    },
                    onDismiss = {
                        clearDialogFields()
                    }
                )
            }
            "buy_now" -> {
                buyNowSuccessDialog(valuesPickerState.value.toIntOrNull() ?: 1)
            }
            else -> {
                when(id){
                    "edit_offer_in_list" -> {
                        val addList =
                            customDialogState.value.fields.find { it.widgetType == "checkbox_group" }?.data
                        val removeList =
                            buildJsonArray {
                                fields.find { it.widgetType == "checkbox_group" }?.choices?.filter {
                                    !addList.toString()
                                        .contains(
                                            it.code.toString()
                                        )
                                }
                                    ?.map { it.code }
                                    ?.fastForEach {
                                        if (it != null) {
                                            add(it)
                                        }
                                    }
                            }
                        _customDialogState.update { dialog ->
                            dialog.copy(
                                fields = buildList {
                                    addAll(
                                        fields.map { field ->
                                            when (field.key) {
                                                "offers_list_ids_add" -> {
                                                    field.copy(
                                                        data = addList
                                                    )
                                                }

                                                "offers_list_ids_remove" -> {
                                                    field.copy(
                                                        data = removeList
                                                    )
                                                }

                                                else -> {
                                                    field.copy()
                                                }
                                            }
                                        })
                                    remove(
                                        fields.find { it.widgetType == "checkbox_group" })
                                }
                            )
                        }
                    }

                    "create_blank_offer_list" -> {
                        idMethod =
                            UserData.login
                        method = "users"
                    }
                }

                customDialogState.value.fields.forEach {
                    if (it.data != null) {
                        body[it.key ?: ""] =
                            it.data!!
                    }
                }

                core.postOperationFields(
                    idMethod,
                    id,
                    method,
                    body = body,
                    onSuccess = {
                        refreshOffer()
                        clearDialogFields()
                    },
                    errorCallback = { errFields ->
                        if (errFields != null) {
                            _customDialogState.update {
                                it.copy(
                                    fields = errFields
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    fun clearDialogFields(){
        _customDialogState.value = CustomDialogState()
    }

    private fun addBid(
        sum: String,
        offer: OfferItem,
        onSuccess: () -> Unit,
        onDismiss: () -> Unit
    ) {
        core.viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                core.operationsMethods.postOperationAdditionalData(
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
                    core.showToast(
                        successToastItem.copy(
                            message = buf.operationResult?.result ?: getString(strings.operationSuccess)
                        )
                    )
                    val eventParameters = mapOf(
                        "lot_id" to offer.id,
                        "lot_name" to offer.title,
                        "lot_city" to offer.location,
                        "auc_delivery" to offer.safeDeal,
                        "lot_category" to offer.catPath.firstOrNull(),
                        "seller_id" to offer.seller.id,
                        "lot_price_start" to offer.price,
                        "buyer_id" to UserData.login,
                        "bid_amount" to sum,
                        "bids_all" to offer.bids?.size
                    )
                    core.analyticsHelper.reportEvent(
                        "bid_made",
                        eventParameters
                    )
                    onSuccess()
                    onDismiss()
                } else {
                    error?.let {  core.onError(it) }
                    onDismiss()
                }
            }
        }
    }

    fun getOffersList(onSuccess: (List<FavoriteListItem>) -> Unit) {
        val offersListOperations = OffersListOperations( core.apiService)
        core.viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.getOffersList() }

            withContext(Dispatchers.Main) {
                val res = data.success
                if (res != null) {
                    val buf = arrayListOf<FavoriteListItem>()
                    buf.addAll(res)
                    onSuccess(res)
                }else{
                    if (data.error != null)
                        core.onError(data.error!!)
                }
            }
        }
    }

    fun openMesDialog() {
        core.viewModelScope.launch {
            if (UserData.token != "") {
                val sellerLabel = getString(strings.sellerLabel)
                val conversationTitle = getString(strings.createConversationLabel)
                val aboutOrder = getString(strings.aboutOfferLabel)
                core.postOperationAdditionalData(
                    offerState.value.id,
                    "checking_conversation_existence",
                    "offers",
                    onSuccess = { body ->
                        val dialogId = body?.operationResult?.additionalData?.conversationId
                        if (dialogId != null) {
                            events.goToDialog(dialogId)
                        } else {
                            val userName = offerState.value.seller.login ?: sellerLabel
                            annotatedTitle.value = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = colors.grayText,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(
                                        conversationTitle
                                    )
                                }

                                withStyle(
                                    SpanStyle(
                                        color = colors.actionTextColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(" $userName ")
                                }

                                withStyle(
                                    SpanStyle(
                                        color = colors.grayText,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(aboutOrder)
                                }

                                withStyle(
                                    SpanStyle(
                                        color = colors.titleTextColor,
                                    )
                                ) {
                                    append(" #${offerState.value.id}")
                                }
                            }
                            
                            _customDialogState.value = CustomDialogState(
                                title = "",
                                fields = emptyList(),
                                typeDialog = "send_message",
                            )
                        }
                    }
                )
            } else {
                goToLogin()
            }
        }
    }

    fun writeToSeller(offerId : Long, messageText : String, onSuccess: (Long?) -> Unit){
        core.viewModelScope.launch(Dispatchers.IO) {
            val res = core.operationsMethods.postOperationAdditionalData(
                offerId,
                "write_to_seller",
                "offers",
                hashMapOf("message" to JsonPrimitive(messageText))
            )
            val buffer1 = res.success
            val error = res.error
            withContext(Dispatchers.Main) {
                if (buffer1 != null) {
                    if (buffer1.operationResult?.result == "ok") {
                        onSuccess(buffer1.body?.toLongOrNull())
                    } else {
                        core.showToast(
                            errorToastItem.copy(
                                message = error?.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                        onSuccess(null)
                    }
                } else {
                    error?.let {  core.onError(it) }
                    onSuccess(null)
                }
            }
        }
    }

    fun getOfferOperations(
        offerId: Long,
        tag : String = "default",
        onSuccess: (List<Operations>) -> Unit
    ) {
        core.viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                offerOperations.getOperationsOffer(offerId, tag)
            }

            withContext(Dispatchers.Main) {
                val buf = res.success?.filter {
                    it.id !in listOf(
                        "add_description",
                        "cloprec107",
                        "make_discount"
                    )
                }

                if (buf != null) {
                    onSuccess(buf)
                }
            }
        }
    }

    fun closeMenu(){
        _isMenuVisible.value = false
    }

    fun onAddBidClick(bid : String){
        if (UserData.token != "") {
            _myMaximalBid.value = bid

            core.viewModelScope.launch {
                val conversationTitle = getString(strings.acceptAddBidsAction)

                _customDialogState.value = CustomDialogState(
                    typeDialog = "add_bid",
                    title = conversationTitle,

                )
            }
        } else {
            goToLogin()
        }
    }
    fun buyNowSuccessDialog(valuesPicker: Int){
        val item = Pair(
            offerState.value.seller.id, listOf(
                SelectedBasketItem(
                    offerId = offerState.value.id,
                    pricePerItem = offerState.value.price.toDouble(),
                    selectedQuantity = valuesPicker
                )
            )
        )
        events.goToCreateOrder(item)
        clearDialogFields()
    }

    fun buyNowClick(){
        if (UserData.token != "") {
            if (offerState.value.quantity > 1) {
                _customDialogState.value = CustomDialogState(
                    typeDialog = "buy_now",
                )
            } else {
                val item = Pair(
                    offerState.value.seller.id, listOf(
                        SelectedBasketItem(
                            offerId = offerState.value.id,
                            pricePerItem = offerState.value.price.toDouble(),
                            selectedQuantity = 1
                        )
                    )
                )
                events.goToCreateOrder(item)
            }
        } else {
            goToLogin()
        }
    }

    @Composable
    fun getDefOperations() : List<MenuItem> {
        val copiedString = stringResource(strings.textCopied)
        return buildList {
            add(
                MenuItem(
                    id = "copyId",
                    title = stringResource(strings.copyOfferId),
                    onClick = {
                        clipBoardEvent(offerState.value.id.toString())
                        core.showToast(
                            successToastItem.copy(
                                message = copiedString
                            )
                        )
                    },
                    icon = drawables.copyIcon
                )
            )

            add(
                MenuItem(
                    id = "share",
                    title = stringResource(strings.shareOffer),
                    onClick = {
                        offerState.value.publicUrl?.let { openShare(it) }
                    },
                    icon = drawables.shareIcon,
                )
            )

            add(
                MenuItem(
                    id = "calendar",
                    title = stringResource(strings.addToCalendar),
                    onClick = {
                        offerState.value.publicUrl?.let { openCalendarEvent(it) }
                    },
                    icon = drawables.calendarIcon,
                )
            )

            if (UserData.token != "") {
                add(
                    MenuItem(
                        id = "create_blank_offer_list",
                        title = stringResource(strings.createNewOffersListLabel),
                        onClick = {
                            getFieldsCreateBlankOfferList { t, f ->
                                _customDialogState.value = CustomDialogState(
                                    title = t,
                                    fields = f,
                                    typeDialog = "create_blank_offer_list",
                                )
                            }
                        },
                        icon = drawables.addFolderIcon,
                    )
                )
            }
        }
    }

    fun getFieldsCreateBlankOfferList(onSuccess: (title: String, List<Fields>) -> Unit){
        core.viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                core.operationsMethods.getOperationFields(
                    UserData.login,
                    "create_blank_offer_list",
                    "users"
                )
            }

            withContext(Dispatchers.Main) {
                val res = data.success
                if (!res?.fields.isNullOrEmpty()){
                    onSuccess(res.description?:"", res.fields)
                }
            }
        }
    }

    fun getAppBarOfferList(): List<NavigationItem> {
        val operations = menuList.value
        return listOf(
            NavigationItem(
                title = "",
                hasNews = false,
                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                badgeCount = null,
                icon = drawables.recycleIcon,
                tint = colors.inactiveBottomNavIconColor,
                onClick = {
                    refreshOffer()
                }
            ),
            NavigationItem(
                title = "edit_offer",
                hasNews = false,
                badgeCount = null,
                isVisible = operations.find { it.id == "edit_offer" } != null,
                icon = drawables.editIcon,
                tint = colors.black,
                onClick = {
                    operations.find { it.id == "edit_offer" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title ="create_note",
                hasNews = false,
                badgeCount = null,
                isVisible = operations.find { it.id == "create_note" || it.id == "edit_note" } != null,
                icon = drawables.editNoteIcon,
                tint = colors.black,
                onClick = {
                    operations.find { it.id == "create_note" || it.id == "edit_note" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = "favorites_btn",
                hasNews = false,
                badgeCount = null,
                isVisible = operations.find { it.id == "watch" || it.id == "unwatch" } != null,
                icon = if (operations.find { it.id == "watch" } == null) drawables.favoritesIconSelected else drawables.favoritesIcon,
                tint = colors.inactiveBottomNavIconColor,
                onClick = {
                    operations.find { it.id == "watch" || it.id == "unwatch" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = "menu",
                hasNews = false,
                badgeCount = null,
                icon = drawables.menuIcon,
                tint = colors.black,
                onClick = {
                    _isMenuVisible.value = true
                }
            ),
        )
    }

    fun isProposalsEnabled() : Boolean {
        return menuList.value.find { it.id == "make_proposal" } != null
    }

    fun setMessageText(text: String){
        _messageText.value = text
    }

    fun setValuesPickerState(text: String) {
        _valuesPickerState.value = text
    }

    fun setFutureTimeInSeconds(text: String) {
        _futureTimeInSeconds.value = text

        _customDialogState.update {
            it.copy(
                fields = it.fields.map { oldField ->
                    if (oldField.key == "future_time"){
                        oldField.copy(
                            data = JsonPrimitive(
                                futureTimeInSeconds.value
                            )
                        )
                    }else{
                        oldField.copy()
                    }
                }
            )
        }
    }

    fun setNewField(field: Fields){
        _customDialogState.update {
            it.copy(
                fields = it.fields.map { oldField ->
                    if (oldField.key == field.key){
                        oldField.copy(
                            data = field.data
                        )
                    }else{
                        oldField.copy()
                    }
                }
            )
        }
    }
}
