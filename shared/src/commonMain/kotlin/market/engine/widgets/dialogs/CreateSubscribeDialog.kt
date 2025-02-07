package market.engine.widgets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateSubscribeDialog(
    isDialogOpen: Boolean,
    title: String,
    onDismiss: () -> Unit,
    goToSubscribe: () -> Unit,
) {

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text(title, style = MaterialTheme.typography.titleSmall)
            },
            text = {},
            confirmButton = {
                SimpleTextButton(
                    text = stringResource(strings.editLabel),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite
                ) {
                    goToSubscribe()
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
