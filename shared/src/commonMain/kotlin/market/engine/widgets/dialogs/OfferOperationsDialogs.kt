package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.dropdown_menu.getDropdownMenu
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferOperationsDialogs(
    offer: Offer,
    title: MutableState<String>,
    fields: MutableState<List<Fields>>,
    choices: List<Choices>,
    errorMes: MutableState<String>,
    showDialog: MutableState<Boolean>,
    showDeleteOfferDialog: MutableState<Boolean>,
    showActivateOfferDialog: MutableState<Boolean>,
    showActivateOfferForFutureDialog: MutableState<Boolean>,
    showCreateNoteDialog: MutableState<String>,
    showOffersListDialog: MutableState<String>,
    showCreatedDialog: MutableState<String>,
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
                                    "lot_city" to offer.freeLocation,
                                    "lot_category" to offer.catpath.lastOrNull(),
                                    "seller_id" to offer.sellerData?.id
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
                                        "lot_name" to offer.title.orEmpty(),
                                        "lot_city" to offer.freeLocation.orEmpty(),
                                        "lot_category" to offer.catpath.lastOrNull(),
                                        "seller_id" to offer.sellerData?.id
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
                                        "lot_name" to offer.title.orEmpty(),
                                        "lot_city" to offer.freeLocation.orEmpty(),
                                        "lot_category" to offer.catpath.lastOrNull(),
                                        "seller_id" to offer.sellerData?.id
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
                val showFields = remember { mutableStateOf(false) }
                viewModel.getOffersList { list ->
                    fields.value.firstOrNull()?.choices = buildList {
                        when (showOffersListDialog.value) {
                            "add_to_list" -> {
                                list.filter { !it.offers.contains(offer.id) }
                            }

                            "remove_from_list" -> {
                                list.filter { it.offers.contains(offer.id) }
                            }

                            else -> list
                        }.fastForEach { item ->
                            add(
                                Choices(
                                    code = JsonPrimitive(item.id),
                                    name = item.title
                                )
                            )
                        }
                    }
                    showFields.value = true
                }

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

                        viewModel.getOffersList { list ->
                            it.firstOrNull()?.choices = buildList {
                                when(showOffersListDialog.value){
                                    "add_to_list" -> {
                                        list.filter { !it.offers.contains(offer.id) }
                                    }
                                    "remove_from_list" -> {
                                        list.filter { it.offers.contains(offer.id) }
                                    }
                                    else -> list
                                }.fastForEach { item ->
                                    add(
                                        Choices(
                                            code = JsonPrimitive(item.id),
                                            name = item.title
                                        )
                                    )
                                }
                            }
                            fields.value = it
                        }
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
}
