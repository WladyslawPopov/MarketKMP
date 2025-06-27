package market.engine.core.repositories

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
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.dialogs.CustomDialogState
import org.jetbrains.compose.resources.getString

class OfferRepository(
    val offer: Offer = Offer(),
    val events: OfferRepositoryEvents,
    val viewModel: BaseViewModel = BaseViewModel(),
)
{
    private val _operationsList = MutableStateFlow<List<MenuItem>>(emptyList())
    val operationsList: StateFlow<List<MenuItem>> = _operationsList.asStateFlow()
    private val _promoList = MutableStateFlow<List<MenuItem>>(emptyList())
    val promoList: StateFlow<List<MenuItem>> = _promoList.asStateFlow()
    private val _customDialogState = MutableStateFlow(CustomDialogState())
    val customDialogState = _customDialogState.asStateFlow()

    val myMaximalBid = MutableStateFlow("")

    val messageText = MutableStateFlow(TextFieldValue(""))

    val valuesPickerState = MutableStateFlow("")

    val futureTimeInSeconds = MutableStateFlow("")
    
    init {
        updateOperations()
    }

    fun updateOperations(){
        viewModel.viewModelScope.launch {
            val currency = getString(strings.currencyCode)
            viewModel.getOfferOperations(
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
                                                offer.catpath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "edit_offer" -> {
                                            events.goToCreateOffer(
                                                CreateOfferType.EDIT,
                                                offer.catpath, offer.id, offer.externalImages
                                            )
                                        }

                                        id == "copy_offer" -> {
                                            events.goToCreateOffer(
                                                CreateOfferType.COPY,
                                                offer.catpath, offer.id, offer.externalImages
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
                                                        viewModel.getOffersList { list ->
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
                                                                            events.update()
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
                                                                                events.update()
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
                                                                                events.update()
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
                                                        "lot_city" to offer.freeLocation,
                                                        "auc_delivery" to offer.safeDeal,
                                                        "lot_category" to offer.catpath.firstOrNull(),
                                                        "seller_id" to offer.sellerData?.id,
                                                        "lot_price_start" to offer.currentPricePerItem,
                                                    )
                                                    viewModel.analyticsHelper.reportEvent(
                                                        "${id}_success",
                                                        eventParameters
                                                    )

                                                    viewModel.updateUserInfo()
                                                    when (operation.id) {
                                                        "watch", "unwatch", "create_blank_offer_list" -> {
                                                            updateOperations()
                                                        }

                                                        else -> {
                                                            events.update()
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

            viewModel.getOfferOperations(
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
                                                    events.update()
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

    fun setCustomDialogState(state: CustomDialogState){
        _customDialogState.value = state
    }

    fun addBid(
        sum: String,
        offer: Offer,
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

    fun openMesDialog() {
        viewModel.viewModelScope.launch {
            if (UserData.token != "") {
                val userName = offer.sellerData?.login ?: getString(strings.sellerLabel)
                val conversationTitle = getString(strings.createConversationLabel)
                val aboutOrder = getString(strings.aboutOfferLabel)
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
                        viewModel.writeToSeller(
                            offer.id, messageText.value.text,
                        ) {
                            events.goToDialog(it)
                            clearDialogFields()
                        }
                    }
                )
            } else {
                events.goToLogin()
            }
        }
    }
    fun onAddBidClick(bid : Long){
        if (UserData.token != "") {
            myMaximalBid.value = bid.toString()

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
                                events.update()
                                clearDialogFields()
                                events.scrollToBids()
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
            offer.sellerData?.id ?: 1L, listOf(
                SelectedBasketItem(
                    offerId = offer.id,
                    pricePerItem = offer.currentPricePerItem?.toDouble()
                        ?: 0.0,
                    selectedQuantity = valuesPicker
                )
            )
        )
        events.goToCreateOrder(item)
        clearDialogFields()
    }
    fun buyNowClick(){
        if (UserData.token != "") {
            if (offer.originalQuantity > 1) {
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
                    offer.sellerData?.id ?: 1L, listOf(
                        SelectedBasketItem(
                            offerId = offer.id,
                            pricePerItem = offer.currentPricePerItem?.toDouble()
                                ?: 0.0,
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
}
