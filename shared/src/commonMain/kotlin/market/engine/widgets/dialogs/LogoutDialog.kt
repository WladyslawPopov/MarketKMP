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
fun LogoutDialog (
    showLogoutDialog : Boolean,
    onDismiss: () -> Unit,
    goToLogin: () -> Unit,
){
    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()
    val userRepository : UserRepository = koinInject()

    if(showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { },
            text = { Text(stringResource(strings.checkForLogoutTitle)) },
            containerColor = colors.white,
            confirmButton = {
                SimpleTextButton(
                    text = stringResource(strings.logoutTitle),
                    backgroundColor = colors.textA0AE,
                    onClick = {
                        analyticsHelper.reportEvent("logout_success", mapOf())
                        userRepository.delete()
                        goToLogin()
                        onDismiss()
                    }
                )
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    onClick = {
                        onDismiss()
                    }
                )
            }
        )
    }
}
