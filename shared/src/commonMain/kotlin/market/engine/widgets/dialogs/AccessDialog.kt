package market.engine.widgets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.repositories.UserRepository
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AccessDialog (
    showDialog : Boolean,
    title : String,
    textAccept : String = stringResource(strings.acceptAction),
    textDecline : String = stringResource(strings.closeWindow),
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
){
    if(showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(title) },
            text = {  },
            containerColor = colors.white,
            confirmButton = {
                SimpleTextButton(
                    text = textAccept,
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite,
                    onClick = {
                        onSuccess()
                        onDismiss()
                    }
                )
            },
            dismissButton = {
                SimpleTextButton(
                    text = textDecline,
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    onClick = {
                        onDismiss()
                    }
                )
            }
        )
    }
}
