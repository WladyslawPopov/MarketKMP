package market.engine.widgets.dropdown_menu

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
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
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.common.clipBoardEvent
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ToastItem
import market.engine.core.network.networkObjects.Operations
import market.engine.core.data.types.ToastType
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.networkObjects.Order
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.dialogs.CommentDialog
import market.engine.widgets.dialogs.CustomDialog
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

    val showDialog = remember { mutableStateOf(false) }
    val listItemMenu : MutableList<Operations> = remember { mutableListOf() }
    val showMenu = remember { mutableStateOf(false) }

    val showCommentDialog = remember { mutableStateOf(false) }
    val reportMode = remember { mutableStateOf(false) }
    val orderType = remember { mutableStateOf(0) }

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

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
                            orderType.value = 1
                            reportMode.value = true
                            showCommentDialog.value = true
                        }
                        "give_feedback_to_buyer" -> {
                            orderType.value = 0
                            reportMode.value = true
                            showCommentDialog.value = true
                        }
                        "set_comment" -> {
                            orderType.value = -1
                            reportMode.value = false
                            showCommentDialog.value = true
                        }

                        "provide_track_id" -> {
                            orderType.value = -2
                            reportMode.value = false
                            showCommentDialog.value = true
                        }

                        "unmark_as_parcel_sent" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    orderOperations.postUnMarkAsParcelSent(
                                        order.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParameters = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )

                                            analyticsHelper.reportEvent(
                                                "unmark_as_parcel_sent",
                                                eventParameters
                                            )
                                            onUpdateMenuItem()
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

                        "mark_as_parcel_sent" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postMarkAsParcelSent(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParams = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )
                                            analyticsHelper.reportEvent("mark_as_parcel_sent", eventParams)
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "unmark_as_payment_sent" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postUnMarkAsPaymentSent(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "mark_as_payment_received" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postMarkAsPaymentSent(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParams = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )
                                            analyticsHelper.reportEvent("mark_as_payment_received", eventParams)
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "mark_as_archived_by_seller" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postMarkAsArchivedBySeller(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParams = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )
                                            analyticsHelper.reportEvent("mark_as_archived_by_seller", eventParams)
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "unmark_as_archived_by_seller" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postUnMarkAsArchivedBySeller(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParams = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )
                                            analyticsHelper.reportEvent("unmark_as_archived_by_seller", eventParams)
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "enable_feedbacks" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postEnableFeedbacks(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "disable_feedbacks" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postDisableFeedbacks(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "remove_feedback_to_buyer" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postRemoveFeedbackToBuyer(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParams = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )
                                            analyticsHelper.reportEvent("remove_feedback_to_buyer", eventParams)
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                        "mark_as_archived_by_buyer" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf = orderOperations.postMarkAsArchivedByBuyer(order.id)
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParams = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )
                                            analyticsHelper.reportEvent("mark_as_archived_by_buyer", eventParams)
                                            onUpdateMenuItem()
                                            onClose()
                                        } else {
                                            errorMes.value = r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    } else {
                                        e?.let { baseViewModel.onError(it) }
                                        onClose()
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    CustomDialog(
        showDialog = showDialog.value,
        title = stringResource(strings.messageAboutError),
        body = { Text(errorMes.value, color = colors.black) },
        onDismiss = { showDialog.value = false },
    )

    CommentDialog(
        isDialogOpen = showCommentDialog.value,
        orderID = order.id,
        orderType = orderType.value,
        commentTextDefault = if (orderType.value >= 0) stringResource(strings.defaultCommentReport) else "",
        onDismiss = {
            showCommentDialog.value = false
            onClose()
        },
        onSuccess = {
            onUpdateMenuItem()
            showCommentDialog.value = false
            onClose()
        },
        baseViewModel = baseViewModel
    )
}
