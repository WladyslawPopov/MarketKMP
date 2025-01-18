package market.engine.widgets.dropdown_menu

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.common.clipBoardEvent
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ToastItem
import market.engine.core.network.networkObjects.Operations
import market.engine.core.data.types.ToastType
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.networkObjects.Order
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Composable
fun getOrderOperations(
    order: Order,
    baseViewModel: BaseViewModel,
    modifier: Modifier = Modifier,
    showCopyId : Boolean = true,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    onUpdateMenuItem: () -> Unit,
    onClose: () -> Unit,
) {
    val scope = baseViewModel.viewModelScope
    val errorMes = remember { mutableStateOf("") }
    val orderOperations : OrderOperations = koinInject()
    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    val showDialog = remember { mutableStateOf(false) }
    val listItemMenu : MutableList<Operations> = remember { mutableListOf() }
    val showMenu = remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        scope.launch {
            val res = orderOperations.getOperationsOrder(order.id)
            withContext(Dispatchers.Main){
                val buf = res.success?.filter {
                    it.id in listOf(
                        "give_feedback_to_seller",
                        "give_feedback_to_buyer",
                        "provide_track_id",
                        "set_comment",
                        "unmark_as_parcel_sent",
                        "mark_as_parcel_sent",
                        "unmark_as_payment_sent",
                        "mark_as_payment_received",
                        "mark_as_archived_by_seller",
                        "unmark_as_archived_by_seller",
                        "enable_feedbacks",
                        "disable_feedbacks",
                        "remove_feedback_to_buyer",
                        "mark_as_archived_by_buyer",
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
                        text = stringResource(strings.copyOrderId),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )
                },
                onClick = {
                    clipBoardEvent(order.id.toString())

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
                    when (operation.id)  {
                        "give_feedback_to_seller" -> {

                        }

                        "give_feedback_to_buyer" -> {

                        }

                        "provide_track_id" -> {

                        }

                        "set_comment" -> {

                        }

                        "unmark_as_parcel_sent" -> {

                        }

                        "mark_as_parcel_sent" -> {

                        }

                        "unmark_as_payment_sent" -> {

                        }

                        "mark_as_payment_received" -> {

                        }

                        "mark_as_archived_by_seller" -> {

                        }

                        "unmark_as_archived_by_seller" -> {

                        }

                        "enable_feedbacks" -> {

                        }

                        "disable_feedbacks" -> {

                        }

                        "remove_feedback_to_buyer" -> {

                        }

                        "mark_as_archived_by_buyer" -> {

                        }
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
}
