package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.networkObjects.Order
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun OrderMessageDialog(
    isDialogOpen: Boolean,
    order: Order,
    type: DealTypeGroup,
    onDismiss: () -> Unit,
    onSuccess: (Long?) -> Unit,
    baseViewModel: BaseViewModel,
) {
    val isShowDialog = remember { mutableStateOf(false) }

    val orderOperations: OrderOperations = koinInject()

    val messageText = remember { mutableStateOf(TextFieldValue()) }


    val userName = remember(order.id) {
        if (type != DealTypeGroup.BUY) {
            order.sellerData?.login ?: ""
        } else {
            order.buyerData?.login ?: ""
        }
    }

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    val conversationTitle = stringResource(strings.createConversationLabel)
    val aboutOrder = stringResource(strings.aboutOrderLabel)

    val titleText = remember(order.id, userName) {
        buildAnnotatedString {
            withStyle(SpanStyle(
                 color = colors.grayText,
                fontWeight = FontWeight.Bold
            )) {
                append(
                    conversationTitle
                )
            }

            withStyle(SpanStyle(
                color = colors.actionTextColor,
                fontWeight = FontWeight.Bold
            )) {
                append(" $userName ")
            }

            withStyle(SpanStyle(
                color = colors.grayText,
                fontWeight = FontWeight.Bold
            )) {
                append(aboutOrder)
            }

            withStyle(SpanStyle(
                color = colors.titleTextColor,
            )) {
                append(" #${order.id}")
            }
        }
    }

    LaunchedEffect(isDialogOpen){
        if (isDialogOpen) {
            val scope = baseViewModel.viewModelScope
            scope.launch(Dispatchers.IO) {
                val res = orderOperations.postCheckingConversationExistence(order.id)
                val dialogId = res.success?.operationResult?.additionalData?.conversationId
                withContext(Dispatchers.Main) {
                    if (dialogId != null) {
                        onSuccess(dialogId)
                    } else {
                        isShowDialog.value = true
                    }
                }
            }
        }
    }

    if (isShowDialog.value) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                isShowDialog.value = false
            },
            title = {
                Text(titleText, style = MaterialTheme.typography.titleSmall)
            },
            text = {
                Column {
                    OutlinedTextInputField(
                        value = messageText.value,
                        onValueChange = {
                            messageText.value = it
                        },
                        label = stringResource(strings.messageLabel),
                        maxSymbols = 2000,
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                val isEnabled = messageText.value.text.isNotBlank()
                val ert = stringResource(strings.operationFailed)

                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    enabled = isEnabled,
                    textColor = colors.alwaysWhite
                ) {
                    val scope = baseViewModel.viewModelScope
                    scope.launch(Dispatchers.IO) {
                        val body = hashMapOf("message" to messageText.value.text)
                        val res = orderOperations.postOrderOperationsWriteToPartner(
                            order.id,
                            body
                        )
                        val buffer1 = res.success
                        val error = res.error
                        withContext(Dispatchers.Main) {
                            if (buffer1 != null) {
                                if (buffer1.operationResult?.result == "ok") {
                                    val eventParameters = mapOf(
                                        "seller_id" to userName,
                                        "buyer_id" to UserData.userInfo?.id.toString(),
                                        "message_type" to "lot",
                                        "order_id" to order.id.toString()
                                    )
                                    analyticsHelper.reportEvent("start_message_to_seller", eventParameters)
                                    onSuccess(buffer1.operationResult.additionalData?.conversationId)
                                    isShowDialog.value = false
                                } else {
                                    baseViewModel.showToast(
                                        errorToastItem.copy(
                                            message = ert
                                        )
                                    )
                                    isShowDialog.value = false
                                    onDismiss()
                                }
                            } else {
                                error?.let { baseViewModel.onError(it) }
                            }
                        }
                    }
                }
            },
            containerColor = colors.white,
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite
                ) {
                    isShowDialog.value = false
                    onDismiss()
                }
            }
        )
    }
}
