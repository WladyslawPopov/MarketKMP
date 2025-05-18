package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.dropdown_menu.getDropdownMenu
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferOperationsDialogs(
    offer: OfferItem,
    title: MutableState<String>,
    fields: MutableState<ArrayList<Fields>>,
    choices: List<Choices>,
    errorMes: MutableState<String>,
    showDialog: MutableState<Boolean>,
    showDeleteOfferDialog: MutableState<Boolean>,
    showActivateOfferDialog: MutableState<Boolean>,
    showActivateOfferForFutureDialog: MutableState<Boolean>,
    showCreateNoteDialog: MutableState<String>,
    showOffersListDialog: MutableState<String>,
    showCreatedDialog: MutableState<String>,
    showPromoDialog: MutableState<String>,
    viewModel : BaseViewModel,
    updateItem: (Long) -> Unit,
    refreshPage: (() -> Unit)?
) {
    val successToast = stringResource(strings.operationSuccess)

    val isClicked = remember { mutableStateOf(false) }
    val scope = remember { viewModel.viewModelScope }
    val offerOperations = remember { viewModel.offerOperations }
    val analyticsHelper = remember { viewModel.analyticsHelper }
    val selected = mutableStateOf(choices.firstOrNull())

    val showFields = remember { mutableStateOf(false) }

    val getOffersListFields : (ArrayList<Fields>) -> Unit = { fields->
        viewModel.getOffersList { list ->
            when (showOffersListDialog.value) {
                "add_to_list" -> {
                    fields.firstOrNull()?.choices = buildList {
                        list.filter { !it.offers.contains(offer.id) }.fastForEach { item ->
                            add(
                                Choices(
                                    code = JsonPrimitive(item.id),
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
                                code = JsonPrimitive(it.id),
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
                    fields.remove(newField)
                    fields.add(newField)
                    fields.fastForEach {
                        if (it.widgetType == "input") {
                            it.widgetType = "hidden"
                        }
                    }
                }
            }
            showFields.value = true
        }
    }

    LaunchedEffect(showOffersListDialog.value) {
        if (showOffersListDialog.value != "") {
            getOffersListFields(fields.value)
        }else{
            showFields.value = false
        }
    }
    
    CustomDialog(
        showDialog = showDialog.value,
        title = stringResource(strings.messageAboutError),
        body = {
            Text(errorMes.value)
        },
        onDismiss = { showDialog.value = false }
    )

    AccessDialog(
        showDialog = showDeleteOfferDialog.value,
        title = stringResource(strings.messageAboutError),
        onDismiss = {
            showDeleteOfferDialog.value = false
            isClicked.value = false
        },
        onSuccess = {
            if(!isClicked.value) {
                isClicked.value = true
                scope.launch {
                    val buf = withContext(Dispatchers.IO) {
                        offerOperations.postOfferOperationsDeleteOffer(
                            offer.id
                        )
                    }
                    val r = buf.success
                    withContext(Dispatchers.Main) {
                        isClicked.value = false
                        if (r != null) {
                            if (r.success) {
                                val eventParam = mapOf(
                                    "lot_id" to offer.id,
                                    "lot_name" to offer.title,
                                    "lot_city" to offer.location,
                                    "lot_category" to offer.catPath.lastOrNull(),
                                    "seller_id" to offer.seller.id
                                )

                                analyticsHelper.reportEvent(
                                    "delete_offer",
                                    eventParameters = eventParam
                                )
                                viewModel.showToast(
                                    successToastItem.copy(
                                        message = successToast
                                    )
                                )

                            } else {
                                errorMes.value = r.humanMessage.toString()
                                showDialog.value = true
                            }
                        }
                    }
                }
                showDeleteOfferDialog.value = false
            }
        }
    )

    CustomDialog(
        showDialog = showActivateOfferDialog.value,
        title = title.value,
        body = {
            getDropdownMenu(
                selectedText = selected.value?.name.toString(),
                selectedTextDef = choices.firstOrNull()?.name.toString(),
                selects = choices.map { it.name.toString() },
                onItemClick = { type ->
                    selected.value = choices.find { it.name == type }
                },
                onClearItem = {
                    selected.value = choices.firstOrNull()
                }
            )
        },
        onDismiss = {
            showActivateOfferDialog.value = false
            isClicked.value = false
        },
        onSuccessful = {
            if(!isClicked.value) {
                isClicked.value = true
                scope.launch {
                    val body = HashMap<String, String>()
                    body["duration"] = selected.value?.code.toString()
                    val buf = withContext(Dispatchers.IO) {
                        offerOperations.postOfferOperationsActivateOffer(
                            offer.id,
                            body
                        )
                    }
                    val r = buf.success
                    withContext(Dispatchers.Main) {
                        isClicked.value = false
                        if (r != null) {
                            if (r.success) {
                                analyticsHelper.reportEvent(
                                    "activate_offer",
                                    eventParameters = mapOf(
                                        "lot_id" to offer.id,
                                        "lot_name" to offer.title,
                                        "lot_city" to offer.location,
                                        "lot_category" to offer.catPath.lastOrNull(),
                                        "seller_id" to offer.seller.id
                                    )
                                )
                                viewModel.showToast(
                                    ToastItem(
                                        isVisible = true,
                                        type = ToastType.SUCCESS,
                                        message = successToast
                                    )
                                )
                                updateItem(offer.id)
                            } else {
                                errorMes.value = r.humanMessage.toString()
                                showDialog.value = true
                            }
                        }
                    }
                }
                showActivateOfferDialog.value = false
            }
        }
    )

    DateDialog(
        showDialog = showActivateOfferForFutureDialog.value,
        isSelectableDates = true,
        onDismiss = {
            showActivateOfferForFutureDialog.value = false
            isClicked.value = false
        },
        onSucceed = { futureTimeInSeconds ->
            if(!isClicked.value) {
                isClicked.value = true
                scope.launch {
                    val body = HashMap<String, Long>()
                    body["future_time"] = futureTimeInSeconds
                    val buf = withContext(Dispatchers.IO) {
                        offerOperations.postOfferOperationsActivateOfferForFuture(
                            offer.id,
                            body
                        )
                    }
                    val r = buf.success
                    withContext(Dispatchers.Main) {
                        isClicked.value = false
                        if (r != null) {
                            if (r.success) {
                                analyticsHelper.reportEvent(
                                    "activate_offer_for_future",
                                    eventParameters = mapOf(
                                        "lot_id" to offer.id,
                                        "lot_name" to offer.title,
                                        "lot_city" to offer.location,
                                        "lot_category" to offer.catPath.lastOrNull(),
                                        "seller_id" to offer.seller.id
                                    )
                                )
                                viewModel.showToast(
                                    ToastItem(
                                        isVisible = true,
                                        type = ToastType.SUCCESS,
                                        message = successToast
                                    )
                                )
                                updateItem(offer.id)
                                showActivateOfferForFutureDialog.value = false

                            } else {
                                errorMes.value = r.humanMessage.toString()
                                showDialog.value = true
                            }
                        }
                    }
                }
            }
        }
    )

    CustomDialog(
        showDialog = showCreateNoteDialog.value != "",
        containerColor = colors.primaryColor,
        title = title.value,
        body = {
            SetUpDynamicFields(fields.value)
        },
        onDismiss = {
            showCreateNoteDialog.value = ""
            isClicked.value = false
        },
        onSuccessful = {
            if(!isClicked.value) {
                isClicked.value = true
                val bodyPost = HashMap<String, JsonElement>()
                fields.value.forEach { field ->
                    if (field.data != null) {
                        bodyPost[field.key ?: ""] = field.data!!
                    }
                }
                viewModel.postNotes(
                    offer.id,
                    showCreateNoteDialog.value,
                    bodyPost,
                    onSuccess = {
                        isClicked.value = false
                        showCreateNoteDialog.value = ""
                        updateItem(offer.id)
                    },
                    onError = {
                        isClicked.value = false
                        fields.value = it
                    }
                )
            }
        }
    )

    CustomDialog(
        showDialog = showOffersListDialog.value != "",
        containerColor = colors.primaryColor,
        title = title.value,
        body = {
            Column {
                if (showFields.value) {
                    SetUpDynamicFields(fields.value)
                }
            }
        },
        onDismiss = {
            showOffersListDialog.value = ""
            isClicked.value = false
        },
        onSuccessful = {
            if(!isClicked.value) {
                isClicked.value = true
                val bodyPost = HashMap<String, JsonElement>()
                when (showOffersListDialog.value) {
                    "edit_offer_in_list" -> {
                        val addList = fields.value.find { it.widgetType == "checkbox_group" }?.data
                        val removeList = buildJsonArray {
                            fields.value.find { it.widgetType == "checkbox_group" }?.choices?.filter {
                                !addList.toString().contains(it.code.toString())
                            }?.map { it.code }?.fastForEach {
                                if (it != null) {
                                    add(it)
                                }
                            }
                        }
                        fields.value.forEach { field ->
                            if (field.widgetType == "hidden") {
                                when(field.key){
                                    "offers_list_ids_add" -> {
                                        field.data = addList
                                    }
                                    "offers_list_ids_remove" -> {
                                        field.data = removeList
                                    }
                                }
                            }
                        }
                        fields.value.remove(fields.value.find { it.widgetType == "checkbox_group" })
                    }
                }

                fields.value.forEach { field ->
                    if (field.data != null) {
                        bodyPost[field.key ?: ""] = field.data!!
                    }
                }

                viewModel.postOfferListFieldForOffer(
                    offer.id,
                    showOffersListDialog.value,
                    bodyPost,
                    onSuccess = {
                        isClicked.value = false
                        showOffersListDialog.value = ""
                        updateItem(offer.id)
                    },
                    onError = {
                        isClicked.value = false

                        getOffersListFields(it)

                        fields.value = it
                    }
                )
            }
        }
    )

    CustomDialog(
        showDialog = showCreatedDialog.value != "",
        containerColor = colors.primaryColor,
        title = title.value,
        body = {
            SetUpDynamicFields(fields.value)
        },
        onDismiss = {
            showCreatedDialog.value = ""
            isClicked.value = false
        },
        onSuccessful = {
            if (!isClicked.value) {
                isClicked.value = true
                val bodyPost = HashMap<String, JsonElement>()
                fields.value.forEach { field ->
                    if (field.data != null) {
                        bodyPost[field.key ?: ""] = field.data!!
                    }
                }

                viewModel.postOfferListFieldForOffer(
                    offer.id,
                    showCreatedDialog.value,
                    bodyPost,
                    onSuccess = {
                        showCreatedDialog.value = ""
                        isClicked.value = false
                        updateItem(offer.id)
                        if (refreshPage != null) {
                            refreshPage()
                        }
                    },
                    onError = { f ->
                        fields.value = f
                        isClicked.value = false
                    }
                )
            }
        }
    )

    CustomDialog(
        showDialog = showPromoDialog.value != "",
        containerColor = colors.primaryColor,
        title = title.value,
        body = {
            SetUpDynamicFields(fields.value)
        },
        onDismiss = {
            showPromoDialog.value = ""
            isClicked.value = false
        },
        onSuccessful = {
            if (!isClicked.value) {
                isClicked.value = true
                val bodyPost = HashMap<String, JsonElement>()
                fields.value.forEach { field ->
                    if (field.data != null) {
                        bodyPost[field.key ?: ""] = field.data!!
                    }
                }
                viewModel.postPromoOperationFields(
                    offer.id,
                    showPromoDialog.value,
                    bodyPost,
                    onSuccess = {
                        showPromoDialog.value = ""
                        isClicked.value = false
                        updateItem(offer.id)
                    },
                    onError = { f ->
                        fields.value = f
                        isClicked.value = false
                    }
                )
            }
        }
    )
}
