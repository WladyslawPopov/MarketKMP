package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.networkObjects.Order
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun CreateOrderDialog(
    isDialogOpen: Boolean,
    order: Order,
    type: DealTypeGroup,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onError: (ServerErrorException?) -> Unit,
    baseViewModel: BaseViewModel,
) {
    val orderOperations: OrderOperations = koinInject()

    val messageText = remember { mutableStateOf("") }

    val charsLeft = 2000 - messageText.value.length


    val userName = remember(order.id) {
        if (type != DealTypeGroup.SELL) {
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

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text(titleText, style = MaterialTheme.typography.titleSmall)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = messageText.value,
                        onValueChange = {
                            if (it.length <= 2000) {
                                messageText.value = it
                            }
                        },
                        label = { Text(stringResource(strings.messageLabel)) },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                text = stringResource(strings.charactersLeftLabel) + " $charsLeft",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.grayText
                            )
                        }
                    )
                }
            },
            confirmButton = {
                val isEnabled = messageText.value.isNotBlank()
                val ert = stringResource(strings.operationFailed)

                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    enabled = isEnabled,
                    textColor = colors.alwaysWhite
                ) {
                    val scope = baseViewModel.viewModelScope
                    scope.launch(Dispatchers.IO) {
                        val body = hashMapOf("message" to messageText.value)
                        val res = orderOperations.postOrderOperationsWriteToPartner(
                            order.id,
                            body
                        )
                        val buffer1 = res.success
                        val error = res.error
                        withContext(Dispatchers.Main) {
                            if (buffer1 != null) {
                                if (buffer1.operationResult?.result == "ok") {
                                    onSuccess()
                                } else {
                                    baseViewModel.showToast(
                                        errorToastItem.copy(
                                            message = ert
                                        )
                                    )
                                    onDismiss()
                                }
                            } else {
                                onError(error)
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
                    onDismiss()
                }
            }
        )
    }
}
