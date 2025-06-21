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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.OfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferMessagingDialog(
    isDialogOpen: Boolean,
    offer: OfferItem,
    onDismiss: () -> Unit,
    onSuccess: (Long?) -> Unit,
    baseViewModel: BaseViewModel,
) {
    val isShowDialog = remember { mutableStateOf(false) }

    val messageText = remember { mutableStateOf(TextFieldValue()) }

    val userName = offer.seller.login ?: stringResource(strings.sellerLabel)

    val conversationTitle = stringResource(strings.createConversationLabel)
    val aboutOrder = stringResource(strings.aboutOfferLabel)

    val titleText = remember {
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
                append(" #${offer.id}")
            }
        }
    }

    LaunchedEffect(isDialogOpen){
        if (isDialogOpen) {
            baseViewModel.postOperationAdditionalData(
                offer.id,
                "checking_conversation_existence",
                "offers",
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
        onSuccessful = {
            baseViewModel.writeToSeller(
                offer.id, messageText.value.text,
            ) {
                onSuccess(it)
                onDismiss()
            }
        },
        onDismiss = {
            isShowDialog.value = false
            onDismiss()
        }
    )
}
