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
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Serializable
data class CustomDialogState(
    val title: String = "",
    val typeDialog: String = "",
    val fields: List<Fields> = emptyList(),
)

@Composable
fun CustomDialog(
    uiState: CustomDialogState,
    isLoading: Boolean = false,
    containerColor: Color = colors.primaryColor,
    annotatedString: AnnotatedString? = null,
    onDismiss: () -> Unit,
    onSuccessful: (() -> Unit)? = null,
    body: @Composable (uiState: CustomDialogState) -> Unit = {},
) {
    val showDialog = uiState.typeDialog
    val title = uiState.title

    if (showDialog != "") {
        AlertDialog(
            containerColor = containerColor,
            tonalElevation = 0.dp,
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    annotatedString ?: AnnotatedString(title),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumnWithScrollBars(
                    containerModifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        body(uiState)
                    }
                }
            },
            confirmButton = {
                if (onSuccessful != null) {
                    SimpleTextButton(
                        text = stringResource(strings.acceptAction),
                        backgroundColor = colors.inactiveBottomNavIconColor,
                        textColor = colors.alwaysWhite,
                        enabled = !isLoading,
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
