package market.engine.widgets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.functions.OfferOperations
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddBidDialog(
    isDialogOpen: Boolean,
    sum: String,
    offerId: Long,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    baseViewModel: BaseViewModel,
) {
    val offerOperations: OfferOperations = koinInject()
    val conversationTitle = stringResource(strings.acceptAddBidsAction)
    val aboutBid = stringResource(strings.placeBetOnTheAmount)
    val currency = stringResource(strings.currencySign)

    val text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = colors.textA0AE,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(
                    aboutBid
                )
                append(": ")
            }

            withStyle(
                SpanStyle(
                color = colors.titleTextColor,
                fontWeight = FontWeight.Bold
            )
            ) {
                append(sum)
                append(currency)
            }
        }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text(conversationTitle, style = MaterialTheme.typography.titleSmall)
            },
            text = {
                Text(text, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                val success = stringResource(strings.operationSuccess)
                val ert = stringResource(strings.operationFailed)

                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite
                ) {
                    val scope = baseViewModel.viewModelScope
                    scope.launch(Dispatchers.IO) {
                        val body = hashMapOf("price" to sum)
                        val res = offerOperations.postOfferOperationsAddBid(
                            offerId,
                            body
                        )
                        val buf = res.success
                        val error = res.error
                        withContext(Dispatchers.Main) {
                            if (buf != null) {
                                if (buf.success) {
                                    baseViewModel.showToast(
                                        successToastItem.copy(
                                            message = buf.humanMessage ?: success
                                        )
                                    )
                                    onSuccess()
                                } else {
                                    baseViewModel.showToast(
                                        errorToastItem.copy(
                                            message = buf.humanMessage ?: ert
                                        )
                                    )
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
                    onDismiss()
                }
            }
        )
    }
}
