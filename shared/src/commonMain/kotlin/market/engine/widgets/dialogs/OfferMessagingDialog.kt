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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.OfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
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

    if (isShowDialog.value) {
        AlertDialog(
            onDismissRequest = {
                isShowDialog.value = false
                onDismiss()
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
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    enabled = isEnabled,
                    textColor = colors.alwaysWhite
                ) {
                    baseViewModel.writeToSeller(
                        offer, messageText.value.text,
                    ) {
                        onSuccess(it)
                        onDismiss()
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
