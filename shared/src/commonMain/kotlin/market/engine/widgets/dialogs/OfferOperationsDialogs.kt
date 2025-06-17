package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.base.SetUpDynamicFields
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferOperationsDialogs(
    offerId : Long,
    title: AnnotatedString,
    fields: ArrayList<Fields>,
    showDialog: String,
    viewModel : BaseViewModel,
    updateItem: (Long) -> Unit,
    close : () -> Unit
) {
    val isClicked = remember { mutableStateOf(false) }
    val showFields = remember { mutableStateOf(false) }

    val getOffersListFields : (ArrayList<Fields>) -> Unit by lazy {
        { fields ->
            viewModel.getOffersList { list ->
                when (showDialog) {
                    "add_to_list" -> {
                        fields.firstOrNull()?.choices = buildList {
                            list.filter { !it.offers.contains(offerId) }.fastForEach { item ->
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
                            list.filter { it.offers.contains(offerId) }.fastForEach { item ->
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

    LaunchedEffect(showDialog) {
        if (showDialog != "") {
            when (showDialog) {
                "edit_offer_in_list", "add_to_list", "remove_from_list" -> {
                    getOffersListFields(fields)
                }
                else -> {
                    showFields.value = true
                }
            }
        }else{
            showFields.value = false
        }
    }

    if (showDialog != "") {
        when (showDialog) {
            "delete_offer" -> {
                AccessDialog(
                    showDialog = showDialog != "",
                    title = title,
                    onDismiss = {
                        isClicked.value = false
                        close()
                    },
                    onSuccess = {
                        if (!isClicked.value) {
                            isClicked.value = true
                            viewModel.postOperationFields(
                                offerId,
                                showDialog,
                                "offers",
                                onSuccess = {
                                    updateItem(offerId)
                                    isClicked.value = false
                                    close()
                                },
                                errorCallback = {
                                    updateItem(offerId)
                                    isClicked.value = false
                                    close()
                                }
                            )
                        }
                    }
                )
            }

            "activate_offer_for_future" -> {
                DateDialog(
                    showDialog = showDialog != "",
                    isSelectableDates = true,
                    onDismiss = {
                        isClicked.value = false
                        close()
                    },
                    onSucceed = { futureTimeInSeconds ->
                        if (!isClicked.value) {
                            isClicked.value = true
                            val body = HashMap<String, JsonElement>()
                            body["future_time"] = JsonPrimitive(futureTimeInSeconds)
                            viewModel.postOperationFields(
                                offerId,
                                showDialog,
                                "offers",
                                body = body,
                                onSuccess = {
                                    updateItem(offerId)
                                    isClicked.value = false
                                    close()
                                },
                                errorCallback = {
                                    isClicked.value = false
                                    close()
                                }
                            )
                        }
                    }
                )
            }

            "error" -> {
                CustomDialog(
                    showDialog = showDialog != "",
                    title = buildAnnotatedString {
                        append(stringResource(strings.messageAboutError))
                    },
                    body = {
                        Text(title)
                    },
                    onDismiss = { close() }
                )
            }

            else -> {
                CustomDialog(
                    showDialog = showDialog != "",
                    title = title,
                    containerColor = colors.grayLayout,
                    body = {
                        Column {
                            if (showFields.value) {
                                SetUpDynamicFields(fields)
                            }
                        }
                    },
                    onDismiss = {
                        isClicked.value = false
                        close()
                    },
                    onSuccessful = {
                        if (!isClicked.value) {
                            var id = offerId

                            var method = "offers"

                            isClicked.value = true
                            val body = HashMap<String, JsonElement>()
                            when (showDialog) {
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
                                    fields.remove(fields.find { it.widgetType == "checkbox_group" })
                                }
                                "create_blank_offer_list" -> {
                                    id = UserData.login
                                    method = "users"
                                }
                            }

                            fields.forEach {
                                if (it.data != null) {
                                    body[it.key ?: ""] = it.data!!
                                }
                            }

                            viewModel.postOperationFields(
                                id,
                                showDialog,
                                method,
                                body = body,
                                onSuccess = {
                                    isClicked.value = false
                                    updateItem(offerId)
                                    close()
                                },
                                errorCallback = { errFields ->
                                    isClicked.value = false
                                    if (errFields != null) {
                                        fields.clear()
                                        fields.addAll(errFields)
                                        getOffersListFields(fields)
                                    }
                                    close()
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
