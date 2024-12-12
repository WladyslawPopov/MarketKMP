package market.engine.widgets.dropdown_menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import market.engine.core.network.functions.OfferOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import market.engine.common.AnalyticsFactory
import market.engine.common.clipBoardEvent
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.items.ToastItem
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Operations
import market.engine.core.types.ToastType
import market.engine.core.util.convertDateYear
import market.engine.core.util.getCurrentDate
import market.engine.presentation.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getOfferOperations(
    offer: Offer,
    baseViewModel: BaseViewModel,
    modifier: Modifier = Modifier,
    showCopyId : Boolean = true,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    onUpdateMenuItem: (Offer) -> Unit,
    onClose: () -> Unit,
) {
    val scope = baseViewModel.viewModelScope
    val errorMes = remember { mutableStateOf("") }
    val offerOperations : OfferOperations = koinInject()
    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    val showDialog = remember { mutableStateOf(false) }

    val showDeleteOfferDialog = remember { mutableStateOf(false) }
    val listItemMenu : MutableList<Operations> = remember { mutableListOf() }
    val showMenu = remember { mutableStateOf(false) }
    val showActivateOfferDialog = remember { mutableStateOf(false) }
    val showActivateOfferForFutureDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        scope.launch(Dispatchers.IO) {
            val res = offerOperations.getOperationsOffer(offer.id)
            withContext(Dispatchers.Main){
                val buf = res.success?.filter {
                    it.id in listOf(
                        "watch",
                        "unwatch",
                        "prolong_offer",
                        "activate_offer_for_future",
                        "activate_offer",
                        "set_anti_sniper",
                        "unset_anti_sniper",
                        "delete_offer",
                        "cancel_all_bids",
                        "remove_bids_of_users",
                        "copy_offer_without_old_photo",
                        "finalize_session",
                        "edit_offer",
                        "copy_offer",
                        "act_on_proposal",
                        "make_proposal",
                        "cancel_all_bids",
                        "remove_bids_of_users"
                    )
                }
                if (buf != null) {
                    listItemMenu.addAll(buf)
                    showMenu.value = true
                }else{
                    showMenu.value = false
                }
            }
        }
    }

    DropdownMenu(
        modifier = modifier.widthIn(max = 350.dp).heightIn(max = 400.dp),
        expanded = showMenu.value,
        onDismissRequest = { onClose() },
        containerColor = colors.white,
        offset = offset
    ) {
        if (showCopyId) {
            val idString = stringResource(strings.idCopied)

            DropdownMenuItem(
                text = {
                    Text(
                        text = idString,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )
                },
                onClick = {
                    clipBoardEvent(offer.id.toString())

                    baseViewModel.showToast(
                        ToastItem(
                            isVisible = true,
                            message = idString,
                            type = ToastType.SUCCESS
                        )
                    )
                    onClose()
                }
            )
        }

        listItemMenu.forEach { operation ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = operation.name ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )
                },
                onClick = {
                    when (operation.id) {
                        "watch" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = offerOperations.postOfferOperationWatch(
                                    offer.id
                                )
                                val response = buf.success
                                withContext(Dispatchers.Main) {
                                    if (response != null) {
                                        if (response.success) {
                                            onUpdateMenuItem(offer)
                                        } else {
                                            errorMes.value =
                                                response.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "unwatch" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = offerOperations.postOfferOperationUnwatch(
                                    offer.id
                                )
                                val response = buf.success
                                withContext(Dispatchers.Main) {
                                    if (response != null) {
                                        if (response.success) {
                                            onUpdateMenuItem(offer)
                                        } else {
                                            errorMes.value =
                                                response.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "prolong_offer" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsProlongOffer(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            onUpdateMenuItem(offer)
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "activate_offer_for_future" -> {
                            showActivateOfferForFutureDialog.value = !showActivateOfferForFutureDialog.value
                        }
                        "activate_offer" -> {
                            showActivateOfferDialog.value = !showActivateOfferDialog.value
                        }
                        "set_anti_sniper" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsSetAntiSniper(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
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
                                                "set_anti_sniper",
                                                eventParam
                                            )
                                            onUpdateMenuItem(offer)
                                            onClose()
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "unset_anti_sniper" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsUnsetAntiSniper(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
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
                                                "unset_anti_sniper",
                                                eventParam
                                            )
                                            onUpdateMenuItem(offer)
                                            onClose()
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "delete_offer" -> {
                            showDeleteOfferDialog.value =
                                !showDeleteOfferDialog.value
                        }
                        "finalize_session" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsFinalizeSession(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            onUpdateMenuItem(offer)
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                        }
                                    }
                                }
                            }
                        }
                        "copy_offer_without_old_photo" -> {}
                        "edit_offer" -> {}
                        "copy_offer" -> {}
                        "act_on_proposal" -> {}
                        "make_proposal" -> {}
                        "cancel_all_bids" -> {}
                        "remove_bids_of_users" -> {}
                    }
                }
            )
        }
    }

    if (showDialog.value) {
        AlertDialog(
            containerColor = colors.white,
            tonalElevation = 0.dp,
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(strings.messageAboutError)) },
            text = { Text(errorMes.value) },
            confirmButton = {
                SimpleTextButton(
                    text = "OK",
                    backgroundColor = colors.grayLayout,
                    onClick = {
                        showDialog.value = false
                        errorMes.value = ""
                        onClose()
                    }
                )
            }
        )
    }
    if (showDeleteOfferDialog.value){
        AlertDialog(
            onDismissRequest = { showDeleteOfferDialog.value = false },
            title = { Text(stringResource(strings.deleteSelectedLot)) },
            text = {  },
            containerColor = colors.white,
            tonalElevation = 0.dp,
            confirmButton = {
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.grayLayout,
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsDeleteOffer(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
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
                                            onUpdateMenuItem(offer)
                                            onClose()
                                        }else{
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                        }
                                    }
                                }
                            }
                        }
                        showDeleteOfferDialog.value = false
                    }
                )
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    onClick = {
                        showDeleteOfferDialog.value = false
                        onClose()
                    }
                )
            }
        )
    }
    if (showActivateOfferDialog.value) {
        val choices = remember{ mutableListOf<Choices>() }
        val title = remember { mutableStateOf("") }
        val selected = remember { mutableStateOf(choices.firstOrNull()) }
        val show = remember { mutableStateOf(false) }

        if (choices.isEmpty()) {
            scope.launch {
                val response = offerOperations.getOfferOperationsActivateOffer(
                    offer.id
                )
                val res = response.success
                if (res != null) {
                    res.firstOrNull()?.let { field ->
                        choices.clear()
                        title.value = field.shortDescription.toString()
                        field.choices?.forEach {
                            choices.add(it)
                        }
                        selected.value = choices.firstOrNull()
                        show.value = true
                    }
                }
            }
        }

        AnimatedVisibility(show.value) {
            AlertDialog(
                containerColor = colors.white,
                tonalElevation = 0.dp,
                onDismissRequest = { showActivateOfferDialog.value = false },
                title = { Text(title.value, style = MaterialTheme.typography.labelSmall) },
                text = {
                    getDropdownMenu(
                        selects = choices.map { it.name.toString() },
                        onItemClick = { type ->
                            selected.value = choices.find { it.name == type }
                        },
                        onClearItem = {
                            selected.value = choices.firstOrNull()
                        }
                    )

                },
                confirmButton = {
                    SimpleTextButton(
                        text = stringResource(strings.acceptAction),
                        backgroundColor = colors.inactiveBottomNavIconColor,
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val body = HashMap<String, String>()
                                    body["duration"] = selected.value?.code.toString()
                                    val buf = offerOperations.postOfferOperationsActivateOffer(
                                        offer.id,
                                        body
                                    )
                                    val r = buf.success
                                    withContext(Dispatchers.Main) {
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
                                                onUpdateMenuItem(offer)
                                                onClose()
                                            } else {
                                                errorMes.value = r.humanMessage.toString()
                                                showDialog.value = true
                                            }
                                        }
                                    }
                                }
                            }
                            showActivateOfferDialog.value = false
                        }
                    )
                },
                dismissButton = {
                    SimpleTextButton(
                        text = stringResource(strings.closeWindow),
                        backgroundColor = colors.grayLayout,
                        onClick = {
                            showActivateOfferDialog.value = false
                            onClose()
                        }
                    )
                }
            )
        }
    }
    if (showActivateOfferForFutureDialog.value) {
        val selectedDate = remember { mutableStateOf<String?>(null) }
        val currentDate = getCurrentDate()

        val currentYear = currentDate.convertDateYear().drop(5)

        val selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis > currentDate.toLong()*1000
            }
        }
        val oneDayInMillis = 24 * 60 * 60 * 1000
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate.toLong()*1000 + oneDayInMillis,
            yearRange = currentYear.toInt()..(currentYear.toInt() + 100),
            selectableDates = selectableDates
        )

        val timePickerState  = rememberTimePickerState(
            is24Hour = true
        )

        DatePickerDialog(
            colors = DatePickerDefaults.colors(
                containerColor = colors.white
            ),
            tonalElevation = 0.dp,
            properties = DialogProperties(usePlatformDefaultWidth = true),
            onDismissRequest = {
                showActivateOfferForFutureDialog.value = false
            },
            confirmButton = {
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    onClick = {
                        if (selectedDate.value == null) {
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                selectedDate.value = selectedDateMillis.toString()
                            }
                        } else {
                            val selectedDateMillis = selectedDate.value?.toLongOrNull() ?: 0L
                            val selectedHour = timePickerState.hour
                            val selectedMinute = timePickerState.minute

                            val localDateTime = Instant
                                .fromEpochMilliseconds(selectedDateMillis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                                .atTime(selectedHour, selectedMinute)

                            val futureTimeInSeconds = localDateTime
                                .toInstant(TimeZone.currentSystemDefault())
                                .epochSeconds

                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val body = HashMap<String, Long>()
                                    body["future_time"] = futureTimeInSeconds
                                    val buf =
                                        offerOperations.postOfferOperationsActivateOfferForFuture(
                                            offer.id,
                                            body
                                        )
                                    val r = buf.success
                                    withContext(Dispatchers.Main) {
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
                                                onUpdateMenuItem(offer)
                                                showActivateOfferForFutureDialog.value = false
                                                onClose()
                                            } else {
                                                errorMes.value = r.humanMessage.toString()
                                                showDialog.value = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(dimens.smallPadding),
                )
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.grayLayout,
                    onClick = {
                        showActivateOfferForFutureDialog.value = false
                        onClose()
                    },
                    modifier = Modifier.padding(dimens.smallPadding),
                )
            }
        ){
            if (selectedDate.value == null) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = null,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.white,
                    ),
                    modifier = Modifier.padding(dimens.smallPadding)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = colors.white,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                        .clip(MaterialTheme.shapes.medium),
                    layoutType = TimePickerLayoutType.Vertical
                )
            }
        }
    }
}
