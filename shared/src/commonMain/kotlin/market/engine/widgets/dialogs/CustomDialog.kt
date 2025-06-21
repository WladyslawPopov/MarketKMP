package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun CustomDialog(
    showDialog: Boolean,
    title: AnnotatedString,
    containerColor: Color = colors.white,
    body: @Composable () -> Unit = {},
    onDismiss: () -> Unit,
    onSuccessful: (() -> Unit) ?= null,
) {
   if (showDialog) {
        AlertDialog(
            containerColor = containerColor,
            tonalElevation = 0.dp,
            onDismissRequest = { onDismiss() },
            title = { Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)) },
            text = {
                LazyColumnWithScrollBars(
                    heightMod = Modifier.fillMaxWidth()
                ) {
                    item {
                        body()
                    }
                }
            },
            confirmButton = {
                if (onSuccessful != null) {
                    SimpleTextButton(
                        text = stringResource(strings.acceptAction),
                        backgroundColor = colors.inactiveBottomNavIconColor,
                        textColor = colors.alwaysWhite,
                        onClick = {
                            onSuccessful()
                        }
                    )
                }
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite,
                    onClick = {
                        onDismiss()
                    }
                )
            }
        )
    }
}
