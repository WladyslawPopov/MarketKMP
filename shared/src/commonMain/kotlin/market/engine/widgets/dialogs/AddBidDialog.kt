package market.engine.widgets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBidDialog(
    isDialogOpen: Boolean,
    sum: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
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
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite
                ) {
                    onSuccess()
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
