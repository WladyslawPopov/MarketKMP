package market.engine.core.repositories

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Operations
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.dialogs.CustomDialogState
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform.getKoin
import kotlin.collections.contains

class OfferRepository(
    val offer: OfferItem = OfferItem(),
    val events: OfferRepositoryEvents,
    val viewModel: CoreViewModel = CoreViewModel(),
)
{
    val offerOperations : OfferOperations by lazy { getKoin().get() }

    private val _operationsList = MutableStateFlow<List<MenuItem>>(emptyList())
    val operationsList: StateFlow<List<MenuItem>> = _operationsList.asStateFlow()

    private val _promoList = MutableStateFlow<List<MenuItem>>(emptyList())
    val promoList: StateFlow<List<MenuItem>> = _promoList.asStateFlow()

    private val _customDialogState = MutableStateFlow(CustomDialogState())
    val customDialogState = _customDialogState.asStateFlow()

    private val _menuList = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuList: StateFlow<List<MenuItem>> = _menuList.asStateFlow()

    private val _myMaximalBid = MutableStateFlow(offer.myMaximalBid)
    val myMaximalBid = _myMaximalBid.asStateFlow()

    private val _messageText = MutableStateFlow(TextFieldValue(""))
    val messageText = _messageText.asStateFlow()

    private val _valuesPickerState = MutableStateFlow("")
    val valuesPickerState = _valuesPickerState.asStateFlow()

    private val _futureTimeInSeconds = MutableStateFlow("")
    val futureTimeInSeconds = _futureTimeInSeconds.asStateFlow()

    private val _isMenuVisible = MutableStateFlow(false)
    val isMenuVisible = _isMenuVisible.asStateFlow()

    private val _isProposalEnabled = MutableStateFlow(false)
    val isProposalEnabled = _isProposalEnabled.asStateFlow()
    
    init {
        updateOperations()
    }

    fun refreshOffer(){
        update()
        events.refreshPage()
    }

    fun update(){
        updateOperations()
        events.updateItem(item = offer)
    }

    fun updateOperations(){
        viewModel.viewModelScope.launch {
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
                                        id == "copy_offer_without_old_photo" -> {
                                            events.goToCreateOffer(
                                                CreateOfferType.COPY_WITHOUT_IMAGE,
                                                offer.catPath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "edit_offer" -> {
                                            events.goToCreateOffer(
                                                CreateOfferType.EDIT,
                                                offer.catPath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "copy_offer" -> {
                                            events.goToCreateOffer(
                                                CreateOfferType.COPY,
                                                offer.catPath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "act_on_proposal" -> {
                                            events.goToProposalPage(
                                                ProposalType.ACT_ON_PROPOSAL
                                            )
                                        }

                                        id == "make_proposal" -> {
                                            events.goToProposalPage(
                                                ProposalType.MAKE_PROPOSAL
                                            )
                                        }

                                        id == "cancel_all_bids" -> {
                                            events.goToDynamicSettings(
                                                "cancel_all_bids",
                                                offer.id
                                            )
                                        }

                                        id == "remove_bids_of_users" -> {
                                            events.goToDynamicSettings(
                                                "remove_bids_of_users",
                                                offer.id
                                            )
                                        }

                                        !isDataless -> {
                                            viewModel.getOperationFields(
                                                offer.id,
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
                                                                    fields.firstOrNull()?.choices = buildList {
                                                                        list.filter { !it.offers.contains(offer.id) }.fastForEach { item ->
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
                                                                    fields.firstOrNull()?.choices = buildList {
                                                                        list.filter { it.offers.contains(offer.id) }.fastForEach { item ->
                                                                            add(
                                                                                Choices(
                                                                                    code = JsonPrimitive(item.id),
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

                                                            _customDialogState.value = CustomDialogState(
                                                                title = AnnotatedString(t),
                                                                fields = fields,
                                                                typeDialog = id,
                                                                onDismiss = {
                                                                    clearDialogFields()
                                                                },
                                                                onSuccessful = {
                                                                    var method = "offers"
                                                                    var idMethod = offer.id

                                                                    val body = HashMap<String, JsonElement>()
                                                                    when (id) {
                                                                        "edit_offer_in_list" -> {
                                                                            val addList =
                                                                                fields.find { it.widgetType == "checkbox_group" }?.data
                                                                            val removeList = buildJsonArray {
                                                                                fields.find { it.widgetType == "checkbox_group" }?.choices?.filter {
                                                                                    !addList.toString().contains(it.code.toString())
                                                                                }?.map { it.code }?.fastForEach {
                                                                                    if (it != null) {
                                                                                        add(it)
                                                                                    }
                                                                                }
                                                                            }
                                                                            fields.forEach { field ->
                                                                                if (field.widgetType == "hidden") {
                                                                                    when (field.key) {
                                                                                        "offers_list_ids_add" -> {
                                                                                            field.data = addList
                                                                                        }

                                                                                        "offers_list_ids_remove" -> {
                                                                                            field.data = removeList
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            fields = buildList {
                                                                                addAll(fields)
                                                                                remove(fields.find { it.widgetType == "checkbox_group" })
                                                                            }
                                                                        }
                                                                        "create_blank_offer_list" -> {
                                                                            idMethod = UserData.login
                                                                            method = "users"
                                                                        }
                                                                    }

                                                                    fields.forEach {
                                                                        if (it.data != null) {
                                                                            body[it.key ?: ""] = it.data!!
                                                                        }
                                                                    }

                                                                    viewModel.postOperationFields(
                                                                        idMethod,
                                                                        id,
                                                                        method,
                                                                        body = body,
                                                                        onSuccess = {
                                                                            clearDialogFields()
                                                                            refreshOffer()
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
                                                            )
                                                        }
                                                    }
                                                    else -> {
                                                        _customDialogState.value = CustomDialogState(
                                                            title = AnnotatedString(t),
                                                            fields = fields,
                                                            typeDialog = id ?: "",
                                                            onDismiss = {
                                                                clearDialogFields()
                                                            },
                                                            onSuccessful = {
                                                                when(id){
                                                                    "activate_offer_for_future" -> {
                                                                        val body =
                                                                            HashMap<String, JsonElement>()
                                                                        body["future_time"] =
                                                                            JsonPrimitive(
                                                                                futureTimeInSeconds.value
                                                                            )
                                                                        viewModel.postOperationFields(
                                                                            offer.id,
                                                                            id,
                                                                            "offers",
                                                                            body = body,
                                                                            onSuccess = {
                                                                                viewModel.setUpdateItem(offer.id)
                                                                                refreshOffer()
                                                                                clearDialogFields()
                                                                            },
                                                                            errorCallback = {
                                                                                clearDialogFields()
                                                                            }
                                                                        )
                                                                    }
                                                                    else -> {
                                                                        val body = HashMap<String, JsonElement>()
                                                                        fields.forEach {
                                                                            if (it.data != null) {
                                                                                body[it.key ?: ""] = it.data!!
                                                                            }
                                                                        }

                                                                        viewModel.postOperationFields(
                                                                            offer.id,
                                                                            id ?: "",
                                                                            "offers",
                                                                            body = body,
                                                                            onSuccess = {
                                                                                viewModel.setUpdateItem(offer.id)
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
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {
                                            viewModel.postOperationFields(
                                                offer.id,
                                                id ?: "",
                                                "offers",
                                                onSuccess = {
                                                    val eventParameters = mapOf(
                                                        "lot_id" to offer.id,
                                                        "lot_name" to offer.title,
                                                        "lot_city" to offer.location,
                                                        "auc_delivery" to offer.safeDeal,
                                                        "lot_category" to offer.catPath.firstOrNull(),
                                                        "seller_id" to offer.seller.id,
                                                        "lot_price_start" to offer.price,
                                                    )
                                                    viewModel.analyticsHelper.reportEvent(
                                                        "${id}_success",
                                                        eventParameters
                                                    )
                                                    when (operation.id) {
                                                        "watch", "unwatch", "create_blank_offer_list" -> {
                                                            viewModel.setUpdateItem(offer.id)
                                                            updateOperations()
                                                        }

                                                        else -> {
                                                            viewModel.setUpdateItem(offer.id)
                                                            refreshOffer()
                                                        }
                                                    }
                                                    viewModel.updateUserInfo()
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
                _isProposalEnabled.value = isProposalsEnabled()
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
                                viewModel.getOperationFields(
                                    offer.id,
                                    operation.id ?: "",
                                    "offers"
                                ) { t, f ->
                                    _customDialogState.value = CustomDialogState(
                                        title = buildAnnotatedString {
                                            append(t)
                                            withStyle(
                                                SpanStyle(
                                                    color = colors.notifyTextColor,
                                                )
                                            ) {
                                                append(" ${operation.price}$currency")
                                            }
                                        },
                                        fields = f,
                                        typeDialog = operation.id ?: "",
                                        onDismiss = {
                                            clearDialogFields()
                                        },
                                        onSuccessful = {
                                            val body = HashMap<String, JsonElement>()

                                            f.forEach {
                                                if (it.data != null) {
                                                    body[it.key ?: ""] = it.data!!
                                                }
                                            }

                                            viewModel.postOperationFields(
                                                offer.id,
                                                operation.id ?: "",
                                                "offers",
                                                body = body,
                                                onSuccess = {
                                                    viewModel.setUpdateItem(offer.id)
                                                    refreshOffer()
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
                                    )
                                }
                            }
                        )
                    })
                }
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
        viewModel.viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                viewModel.operationsMethods.postOperationAdditionalData(
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
                    viewModel.showToast(
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
                    viewModel.analyticsHelper.reportEvent(
                        "bid_made",
                        eventParameters
                    )
                    onSuccess()
                    onDismiss()
                } else {
                    error?.let { viewModel.onError(it) }
                    onDismiss()
                }
            }
        }
    }

    fun getOffersList(onSuccess: (List<FavoriteListItem>) -> Unit) {
        val offersListOperations = OffersListOperations(viewModel.apiService)
        viewModel.viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.getOffersList() }

            withContext(Dispatchers.Main) {
                val res = data.success
                if (res != null) {
                    val buf = arrayListOf<FavoriteListItem>()
                    buf.addAll(res)
                    onSuccess(res)
                }else{
                    if (data.error != null)
                        viewModel.onError(data.error!!)
                }
            }
        }
    }

    fun openMesDialog() {
        viewModel.viewModelScope.launch {
            if (UserData.token != "") {
                val sellerLabel = getString(strings.sellerLabel)
                val conversationTitle = getString(strings.createConversationLabel)
                val aboutOrder = getString(strings.aboutOfferLabel)
                viewModel.postOperationAdditionalData(
                    offer.id,
                    "checking_conversation_existence",
                    "offers",
                    onSuccess = { body ->
                        val dialogId = body?.operationResult?.additionalData?.conversationId
                        if (dialogId != null) {
                            events.goToDialog(dialogId)
                        } else {
                            val userName = offer.seller.login ?: sellerLabel
                            _customDialogState.value = CustomDialogState(
                                title = buildAnnotatedString {
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
                                        append(" #${offer.id}")
                                    }
                                },
                                fields = emptyList(),
                                typeDialog = "send_message",
                                onDismiss = {
                                    clearDialogFields()
                                },
                                onSuccessful = {
                                    writeToSeller(
                                        offer.id, messageText.value.text,
                                    ) {
                                        events.goToDialog(it)
                                        clearDialogFields()
                                    }
                                }
                            )
                        }
                    }
                )
            } else {
                events.goToLogin()
            }
        }
    }

    fun writeToSeller(offerId : Long, messageText : String, onSuccess: (Long?) -> Unit){
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val res = viewModel.operationsMethods.postOperationAdditionalData(
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
                        viewModel.showToast(
                            errorToastItem.copy(
                                message = error?.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                        onSuccess(null)
                    }
                } else {
                    error?.let { viewModel.onError(it) }
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
        viewModel.viewModelScope.launch {
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

            viewModel.viewModelScope.launch {
                val conversationTitle = getString(strings.acceptAddBidsAction)

                _customDialogState.value = CustomDialogState(
                    typeDialog = "add_bid",
                    title = AnnotatedString(conversationTitle),
                    onDismiss = {
                        clearDialogFields()
                    },
                    onSuccessful = {
                        addBid(
                            myMaximalBid.value,
                            offer,
                            onSuccess = {
                                update()
                                events.scrollToBids()
                                clearDialogFields()
                            },
                            onDismiss = {
                                clearDialogFields()
                            }
                        )
                    }
                )
            }
        } else {
            events.goToLogin()
        }
    }
    fun buyNowSuccessDialog(valuesPicker: Int){
        val item = Pair(
            offer.seller.id, listOf(
                SelectedBasketItem(
                    offerId = offer.id,
                    pricePerItem = offer.price.toDouble(),
                    selectedQuantity = valuesPicker
                )
            )
        )
        events.goToCreateOrder(item)
        clearDialogFields()
    }
    fun buyNowClick(){
        if (UserData.token != "") {
            if (offer.quantity > 1) {
                _customDialogState.value = CustomDialogState(
                    typeDialog = "buy_now",
                    onDismiss = {
                        clearDialogFields()
                    },
                    onSuccessful = {
                        buyNowSuccessDialog(valuesPickerState.value.toIntOrNull() ?: 1)
                    }
                )
            } else {
                val item = Pair(
                    offer.seller.id, listOf(
                        SelectedBasketItem(
                            offerId = offer.id,
                            pricePerItem = offer.price.toDouble(),
                            selectedQuantity = 1
                        )
                    )
                )
                events.goToCreateOrder(item)
            }
        } else {
            events.goToLogin()
        }
    }

    suspend fun getDefOperations() : List<MenuItem> {
        val copiedString = getString(strings.textCopied)
        return buildList {
            add(
                MenuItem(
                    id = "copyId",
                    title = getString(strings.copyOfferId),
                    icon = drawables.copyIcon,
                    onClick = {
                        clipBoardEvent(offer.id.toString())
                        viewModel.showToast(
                            successToastItem.copy(
                                message = copiedString
                            )
                        )
                    }
                )
            )

            add(
                MenuItem(
                    id = "share",
                    title = getString(strings.shareOffer),
                    icon = drawables.shareIcon,
                    onClick = {
                        offer.publicUrl?.let { openShare(it) }
                    }
                ))

            add(
                MenuItem(
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
                                _customDialogState.value = CustomDialogState(
                                    title = AnnotatedString(t),
                                    fields = f,
                                    typeDialog = "create_blank_offer_list",
                                    onDismiss = {
                                        clearDialogFields()
                                    },
                                    onSuccessful = {
                                        val body = HashMap<String, JsonElement>()

                                        f.forEach {
                                            if (it.data != null) {
                                                body[it.key ?: ""] = it.data!!
                                            }
                                        }

                                        viewModel.postOperationFields(
                                            UserData.login,
                                            "create_blank_offer_list",
                                            "users",
                                            body = body,
                                            onSuccess = {
                                                refreshOffer()
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
                                )
                            }
                        }
                    )
                )
            }
        }
    }

    fun getFieldsCreateBlankOfferList(onSuccess: (title: String, List<Fields>) -> Unit){
        viewModel.viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                viewModel.operationsMethods.getOperationFields(
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

    @Composable
    fun getAppBarOfferList(): List<NavigationItem> {
        val operations = operationsList.value
        return listOf(
            NavigationItem(
                title = "",
                icon = drawables.recycleIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                badgeCount = null,
                onClick = {
                    refreshOffer()
                }
            ),
            NavigationItem(
                title = stringResource(strings.editLabel),
                icon = drawables.editIcon,
                tint = colors.black,
                hasNews = false,
                badgeCount = null,
                isVisible = operations.find { it.id == "edit_offer" } != null,
                onClick = {
                    operations.find { it.id == "edit_offer" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = stringResource(strings.myNotesTitle),
                icon = drawables.editNoteIcon,
                tint = colors.black,
                hasNews = false,
                badgeCount = null,
                isVisible = operations.find { it.id == "create_note" || it.id == "edit_note" } != null,
                onClick = {
                    operations.find { it.id == "create_note" || it.id == "edit_note" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = stringResource(strings.favoritesTitle),
                icon = if (operations.find { it.id == "watch" } == null) drawables.favoritesIconSelected else drawables.favoritesIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                badgeCount = null,
                isVisible = operations.find { it.id == "watch" || it.id == "unwatch" } != null,
                onClick = {
                    operations.find { it.id == "watch" || it.id == "unwatch" }?.onClick?.invoke()
                }
            ),
            NavigationItem(
                title = stringResource(strings.menuTitle),
                icon = drawables.menuIcon,
                tint = colors.black,
                hasNews = false,
                badgeCount = null,
                onClick = {
                    _isMenuVisible.value = true
                }
            )
        )
    }

    fun isProposalsEnabled() : Boolean {
        return operationsList.value.find { it.id == "make_proposal" } != null
    }

    fun setMessageText(text: TextFieldValue){
        _messageText.value = text
    }

    fun setValuesPickerState(text: String) {
        _valuesPickerState.value = text
    }

    fun setFutureTimeInSeconds(text: String) {
        _futureTimeInSeconds.value = text
    }
}
