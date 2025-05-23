package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.network.networkObjects.Order
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

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
    val messageText = remember { mutableStateOf(TextFieldValue()) }

    val userName = remember(order.id) {
        if (type != DealTypeGroup.BUY) {
            order.sellerData?.login ?: ""
        } else {
            order.buyerData?.login ?: ""
        }
    }

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
            baseViewModel.postOperationAdditionalData(
                order.id,
                "checking_conversation_existence",
                "orders",
                onSuccess = {
                    val dialogId = it?.operationResult?.additionalData?.conversationId
                    if (dialogId != null) {
                        onSuccess(dialogId)
                    } else {
                        isShowDialog.value = true
                    }
                }
            )
        }
    }

    CustomDialog(
        isShowDialog.value,
        title = titleText,
        containerColor = colors.white,
        body = {
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
        onDismiss = {
            isShowDialog.value = false
            onDismiss()
        },
        onSuccessful = {
            baseViewModel.postOperationAdditionalData(
                order.id,
                "write_to_partner",
                "orders",
                hashMapOf("message" to JsonPrimitive(messageText.value.text)),
                onSuccess = {
                    val dialogId = it?.operationResult?.additionalData?.conversationId
                    onSuccess(dialogId)
                    onDismiss()
                }
            )
        }
    )
}
