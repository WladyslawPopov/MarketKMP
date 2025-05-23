package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.util.fastForEach
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.base.SetUpDynamicFields
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferOperationsDialogs(
    offer: OfferItem,
    title: MutableState<AnnotatedString>,
    fields: MutableState<ArrayList<Fields>>,
    showDialog: MutableState<String>,
    viewModel : BaseViewModel,
    updateItem: (Long) -> Unit,
) {
    val isClicked = remember { mutableStateOf(false) }
    val showFields = remember { mutableStateOf(false) }

    val getOffersListFields : (ArrayList<Fields>) -> Unit by lazy {
        { fields ->
            viewModel.getOffersList { list ->
                when (showDialog.value) {
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
    }

    LaunchedEffect(showDialog.value) {
        if (showDialog.value != "") {
            when (showDialog.value) {
                "edit_offer_in_list", "add_to_list", "remove_from_list" -> {
                    getOffersListFields(fields.value)
                }
                else -> {
                    showFields.value = true
                }
            }
        }else{
            showFields.value = false
        }
    }

    if (showDialog.value != "") {
        when (showDialog.value) {
            "delete_offer" -> {
                AccessDialog(
                    showDialog = showDialog.value != "",
                    title = title.value,
                    onDismiss = {
                        showDialog.value = ""
                        isClicked.value = false
                    },
                    onSuccess = {
                        if (!isClicked.value) {
                            isClicked.value = true
                            viewModel.postOperationFields(
                                offer.id,
                                showDialog.value,
                                "offers",
                                onSuccess = {
                                    showDialog.value = ""
                                    updateItem(offer.id)
                                    isClicked.value = false
                                },
                                errorCallback = {
                                    showDialog.value = ""
                                    updateItem(offer.id)
                                    isClicked.value = false
                                }
                            )
                        }
                    }
                )
            }

            "activate_offer_for_future" -> {
                DateDialog(
                    showDialog = showDialog.value != "",
                    isSelectableDates = true,
                    onDismiss = {
                        showDialog.value = ""
                        isClicked.value = false
                    },
                    onSucceed = { futureTimeInSeconds ->
                        if (!isClicked.value) {
                            isClicked.value = true
                            val body = HashMap<String, JsonElement>()
                            body["future_time"] = JsonPrimitive(futureTimeInSeconds)
                            viewModel.postOperationFields(
                                offer.id,
                                showDialog.value,
                                "offers",
                                body = body,
                                onSuccess = {
                                    updateItem(offer.id)
                                    showDialog.value = ""
                                    isClicked.value = false
                                },
                                errorCallback = {
                                    showDialog.value = ""
                                    isClicked.value = false
                                }
                            )
                        }
                    }
                )
            }

            "error" -> {
                CustomDialog(
                    showDialog = showDialog.value != "",
                    title = buildAnnotatedString {
                        append(stringResource(strings.messageAboutError))
                    },
                    body = {
                        Text(title.value)
                    },
                    onDismiss = { showDialog.value = "" }
                )
            }

            else -> {
                CustomDialog(
                    showDialog = showDialog.value != "",
                    title = title.value,
                    containerColor = colors.grayLayout,
                    body = {
                        Column {
                            if (showFields.value) {
                                SetUpDynamicFields(fields.value)
                            }
                        }
                    },
                    onDismiss = {
                        showDialog.value = ""
                        isClicked.value = false
                    },
                    onSuccessful = {
                        if (!isClicked.value) {
                            var id = offer.id

                            var method = "offers"

                            isClicked.value = true
                            val body = HashMap<String, JsonElement>()
                            when (showDialog.value) {
                                "edit_offer_in_list" -> {
                                    val addList =
                                        fields.value.find { it.widgetType == "checkbox_group" }?.data
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
                                    fields.value.remove(fields.value.find { it.widgetType == "checkbox_group" })
                                }
                                "create_blank_offer_list" -> {
                                    id = UserData.login
                                    method = "users"
                                }
                            }

                            fields.value.forEach {
                                if (it.data != null) {
                                    body[it.key ?: ""] = it.data!!
                                }
                            }

                            viewModel.postOperationFields(
                                id,
                                showDialog.value,
                                method,
                                body = body,
                                onSuccess = {
                                    showDialog.value = ""
                                    isClicked.value = false
                                    updateItem(offer.id)
                                },
                                errorCallback = { errFields ->
                                    showDialog.value = ""
                                    isClicked.value = false
                                    if (errFields != null) {
                                        fields.value.clear()
                                        fields.value.addAll(errFields)
                                        getOffersListFields(fields.value)
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
