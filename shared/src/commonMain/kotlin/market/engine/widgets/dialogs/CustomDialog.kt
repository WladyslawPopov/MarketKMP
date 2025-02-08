package market.engine.widgets.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun CustomDialog(
    showDialog: Boolean,
    title: String,
    body: @Composable () -> Unit = {},
    onDismiss: () -> Unit,
    onSuccessful: (() -> Unit) ?= null,
) {
    AnimatedVisibility(
        showDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            containerColor = colors.white,
            tonalElevation = 0.dp,
            onDismissRequest = { onDismiss() },
            title = { Text(title, style = MaterialTheme.typography.labelSmall) },
            text = {
                body()
            },
            confirmButton = {
                if (onSuccessful != null) {
                    SimpleTextButton(
                        text = stringResource(strings.acceptAction),
                        backgroundColor = colors.inactiveBottomNavIconColor,
                        onClick = {
                            onSuccessful()
                        }
                    )
                }
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.grayLayout,
                    onClick = {
                        onDismiss()
                    }
                )
            }
        )
    }
}
