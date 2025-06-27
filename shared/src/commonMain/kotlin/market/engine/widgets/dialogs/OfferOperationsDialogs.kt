package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.textFields.OutlinedTextInputField
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource
import kotlin.toString

//class OfferOperationsDialogsViewModel(
//    val offerId : Long,
//    initFields: List<Fields>,
//) : BaseViewModel() {
//    val fields = mutableStateOf(ArrayList(initFields))
//    val isClicked = mutableStateOf(false)
//    val showFields = mutableStateOf(false)
//
//    val getOffersListFields : (ArrayList<Fields>) -> Unit by lazy {
//        { choicesFields ->
//            viewModel.getOffersList { list ->
//                when (showDialog) {
//                    "add_to_list" -> {
//                        choicesFields.firstOrNull()?.choices = buildList {
//                            list.filter { !it.offers.contains(offerId) }.fastForEach { item ->
//                                add(
//                                    Choices(
//                                        code = JsonPrimitive(item.id),
//                                        name = item.title
//                                    )
//                                )
//                            }
//                        }
//                    }
//
//                    "remove_from_list" -> {
//                        choicesFields.firstOrNull()?.choices = buildList {
//                            list.filter { it.offers.contains(offerId) }.fastForEach { item ->
//                                add(
//                                    Choices(
//                                        code = JsonPrimitive(item.id),
//                                        name = item.title
//                                    )
//                                )
//                            }
//                        }
//                    }
//
//                    "edit_offer_in_list" -> {
//                        val newField = Fields(
//                            widgetType = "checkbox_group",
//                            choices = list.map {
//                                Choices(
//                                    code = JsonPrimitive(it.id),
//                                    name = it.title
//                                )
//                            },
//                            data = choicesFields.firstOrNull()?.data,
//                            key = choicesFields.firstOrNull()?.key,
//                            errors = choicesFields.firstOrNull()?.errors,
//                            shortDescription = choicesFields.firstOrNull()?.shortDescription,
//                            longDescription = choicesFields.firstOrNull()?.longDescription,
//                            validators = choicesFields.firstOrNull()?.validators,
//                        )
//                        choicesFields.remove(newField)
//                        choicesFields.add(newField)
//                        choicesFields.fastForEach {
//                            if (it.widgetType == "input") {
//                                it.widgetType = "hidden"
//                            }
//                        }
//                    }
//                }
//                showFields.value = true
//            }
//        }
//    }
//
//    init {
//        if (showDialog != "") {
//            when (showDialog) {
//                "edit_offer_in_list", "add_to_list", "remove_from_list" -> {
//                    getOffersListFields(fields.value)
//                }
//                else -> {
//                    showFields.value = true
//                }
//            }
//        }else{
//            showFields.value = false
//        }
//    }
//}

@Composable
fun OfferOperationsDialogs(
    offerId : Long,
    title: AnnotatedString,
    initFields: List<Fields>,
    showDialog: String,
    viewModel : BaseViewModel,
    updateItem: (Long) -> Unit,
    onSuccess: (Long?) -> Unit = {},
    close : (fullRefresh : Boolean) -> Unit
) {
//    val fields = mutableStateOf(ArrayList(initFields))
//    val isClicked = remember { mutableStateOf(false) }
//    val showFields = remember { mutableStateOf(false) }
//
//    val getOffersListFields : (ArrayList<Fields>) -> Unit by lazy {
//        { choicesFields ->
//            viewModel.getOffersList { list ->
//                when (showDialog) {
//                    "add_to_list" -> {
//                        choicesFields.firstOrNull()?.choices = buildList {
//                            list.filter { !it.offers.contains(offerId) }.fastForEach { item ->
//                                add(
//                                    Choices(
//                                        code = JsonPrimitive(item.id),
//                                        name = item.title
//                                    )
//                                )
//                            }
//                        }
//                    }
//
//                    "remove_from_list" -> {
//                        choicesFields.firstOrNull()?.choices = buildList {
//                            list.filter { it.offers.contains(offerId) }.fastForEach { item ->
//                                add(
//                                    Choices(
//                                        code = JsonPrimitive(item.id),
//                                        name = item.title
//                                    )
//                                )
//                            }
//                        }
//                    }
//
//                    "edit_offer_in_list" -> {
//                        val newField = Fields(
//                            widgetType = "checkbox_group",
//                            choices = list.map {
//                                Choices(
//                                    code = JsonPrimitive(it.id),
//                                    name = it.title
//                                )
//                            },
//                            data = choicesFields.firstOrNull()?.data,
//                            key = choicesFields.firstOrNull()?.key,
//                            errors = choicesFields.firstOrNull()?.errors,
//                            shortDescription = choicesFields.firstOrNull()?.shortDescription,
//                            longDescription = choicesFields.firstOrNull()?.longDescription,
//                            validators = choicesFields.firstOrNull()?.validators,
//                        )
//                        choicesFields.remove(newField)
//                        choicesFields.add(newField)
//                        choicesFields.fastForEach {
//                            if (it.widgetType == "input") {
//                                it.widgetType = "hidden"
//                            }
//                        }
//                    }
//                }
//                showFields.value = true
//            }
//        }
//    }
//
//    LaunchedEffect(showDialog) {
//        if (showDialog != "") {
//            when (showDialog) {
//                "edit_offer_in_list", "add_to_list", "remove_from_list" -> {
//                    getOffersListFields(fields.value)
//                }
//                else -> {
//                    showFields.value = true
//                }
//            }
//        }else{
//            showFields.value = false
//        }
//    }
//
//    if (showDialog != "") {
//        when (showDialog) {
//            "send_message" -> {
//                val messageText = remember { mutableStateOf(TextFieldValue()) }
//
////                CustomDialog(
////                    showDialog != "",
////                    title = title,
////                    containerColor = colors.white,
////                    body = {
////                        Column {
////                            OutlinedTextInputField(
////                                value = messageText.value,
////                                onValueChange = {
////                                    messageText.value = it
////                                },
////                                label = stringResource(strings.messageLabel),
////                                maxSymbols = 2000,
////                                singleLine = false
////                            )
////                        }
////                    },
////                    onSuccessful = {
////                        viewModel.writeToSeller(
////                            offerId, messageText.value.text,
////                        ) {
////                            onSuccess(it)
////                            close(false)
////                        }
////                    },
////                    onDismiss = {
////                        close(false)
////                    }
////                )
//            }
//
//            "activate_offer_for_future" -> {
//                DateDialog(
//                    showDialog = showDialog != "",
//                    isSelectableDates = true,
//                    onDismiss = {
//                        isClicked.value = false
//                        close(false)
//                    },
//                    onSucceed = { futureTimeInSeconds ->
//                        if (!isClicked.value) {
//                            isClicked.value = true
//                            val body = HashMap<String, JsonElement>()
//                            body["future_time"] = JsonPrimitive(futureTimeInSeconds)
//                            viewModel.postOperationFields(
//                                offerId,
//                                showDialog,
//                                "offers",
//                                body = body,
//                                onSuccess = {
//                                    updateItem(offerId)
//                                    isClicked.value = false
//                                    close(false)
//                                },
//                                errorCallback = {
//                                    isClicked.value = false
//                                    close(false)
//                                }
//                            )
//                        }
//                    }
//                )
//            }
//
//            "error" -> {
//                CustomDialog(
//                    showDialog = showDialog != "",
//                    title = buildAnnotatedString {
//                        append(stringResource(strings.messageAboutError))
//                    },
//                    body = {
//                        Text(title)
//                    },
//                    onDismiss = { close(false) }
//                )
//            }
//
//            "add_bid" -> {
//                AddBidDialog(
//                    showDialog.value == "add_bid",
//                    myMaximalBid.value,
//                    onDismiss = {
//                        viewModel.clearDialogFields()
//                    },
//                    onSuccess = {
//                        viewModel.addBid(
//                            myMaximalBid.value,
//                            offer,
//                            onSuccess = {
//                                viewModel.updateBidsInfo(offer)
//                                viewModel.clearDialogFields()
//                                scope.launch {
//                                    stateColumn.animateScrollToItem(goToBids)
//                                }
//                            },
//                            onDismiss = {
//                                viewModel.clearDialogFields()
//                            }
//                        )
//                    },
//                )
//            }
//
//            "buy_now" -> {
//                CustomDialog(
//                    showDialog = showDialog.value == "buy_now",
//                    title = AnnotatedString(""),
//                    body = {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            SeparatorLabel(
//                                stringResource(strings.chooseAmountLabel)
//                            )
//
//                            Row(
//                                modifier = Modifier.fillMaxWidth()
//                                    .padding(dimens.mediumPadding),
//                                horizontalArrangement = Arrangement.Center,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                ListPicker(
//                                    state = valuesPickerState,
//                                    items = uiState.value.buyNowCounts,
//                                    visibleItemsCount = 3,
//                                    modifier = Modifier.fillMaxWidth(0.5f),
//                                    textModifier = Modifier.padding(dimens.smallPadding),
//                                    textStyle = MaterialTheme.typography.titleLarge,
//                                    dividerColor = colors.textA0AE
//                                )
//                            }
//                        }
//                    },
//                    onSuccessful = {
//                        viewModel.buyNowSuccessDialog(offer, valuesPickerState.selectedItem.toIntOrNull() ?: 1)
//                    },
//                    onDismiss = {
//                        viewModel.clearDialogFields()
//                    }
//                )
//
//            }
//
//            else -> {
//                CustomDialog(
//                    showDialog = showDialog != "",
//                    title = title,
//                    containerColor = colors.grayLayout,
//                    body = {
//                        Column {
//                            if (showFields.value) {
//                                SetUpDynamicFields(fields.value)
//                            }
//                        }
//                    },
//                    onDismiss = {
//                        isClicked.value = false
//                        close(false)
//                    },
//                    onSuccessful = {
//                        if (!isClicked.value) {
//                            var id = offerId
//
//                            var method = "offers"
//
//                            isClicked.value = true
//                            val body = HashMap<String, JsonElement>()
//                            when (showDialog) {
//                                "edit_offer_in_list" -> {
//                                    val addList =
//                                        fields.value.find { it.widgetType == "checkbox_group" }?.data
//                                    val removeList = buildJsonArray {
//                                        fields.value.find { it.widgetType == "checkbox_group" }?.choices?.filter {
//                                            !addList.toString().contains(it.code.toString())
//                                        }?.map { it.code }?.fastForEach {
//                                            if (it != null) {
//                                                add(it)
//                                            }
//                                        }
//                                    }
//                                    fields.value.forEach { field ->
//                                        if (field.widgetType == "hidden") {
//                                            when (field.key) {
//                                                "offers_list_ids_add" -> {
//                                                    field.data = addList
//                                                }
//
//                                                "offers_list_ids_remove" -> {
//                                                    field.data = removeList
//                                                }
//                                            }
//                                        }
//                                    }
//                                    fields.value.remove(fields.value.find { it.widgetType == "checkbox_group" })
//                                }
//                                "create_blank_offer_list" -> {
//                                    id = UserData.login
//                                    method = "users"
//                                }
//                            }
//
//                            fields.value.forEach {
//                                if (it.data != null) {
//                                    body[it.key ?: ""] = it.data!!
//                                }
//                            }
//
//                            viewModel.postOperationFields(
//                                id,
//                                showDialog,
//                                method,
//                                body = body,
//                                onSuccess = {
//                                    isClicked.value = false
//                                    updateItem(offerId)
//                                    close(showDialog == "create_blank_offer_list" || showDialog == "edit_note")
//                                },
//                                errorCallback = { errFields ->
//                                    isClicked.value = false
//                                    if (errFields != null) {
//                                        fields.value.clear()
//                                        fields.value.addAll(errFields)
//                                        getOffersListFields(fields.value)
//                                    }
//                                }
//                            )
//                        }
//                    }
//                )
//            }
//        }
//    }
}
