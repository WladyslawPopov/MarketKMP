package market.engine.fragments.base

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.network.ServerErrorException
import market.engine.core.repositories.UserRepository
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.buttons.SimpleTextButton
import org.koin.compose.koinInject

@Composable
fun onError(
    error : ServerErrorException,
    onRefresh: () -> Unit,
) {
    val humanMessage = error.humanMessage
    val errorCode = error.errorCode

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    val userRepository : UserRepository = koinInject()

    val showDialog = remember { mutableStateOf(false) }

    errorCode.let {
        when{
            (it.contains("Unable to resolve host") || it.contains("failed to connect to") || it.contains("Failed to connect")) -> {
                showNoInternetLayout(onRefresh)
            }
            it == "MISSING_OR_INVALID_TOKEN" -> {
                userRepository.delete()
                goToLogin()
            }
            it.contains(" timeout ") -> {
                showNoInternetLayout(onRefresh)
            }
            it == "[HttpClient] could not ensure Request was active: cancelled=true"->{

            }
            it == "NO_API_KEY" ->{
                showErrLayout(it,onRefresh)
            }
            it == "BAD_API_KEY" ->{
                showErrLayout(it,onRefresh)
            }
            it == "REQUEST_NOT_ALLOWED" ->{
                showErrLayout(it,onRefresh)
            }
            it == "NEEDS_PASSWORD_RESET" ->{
                goToDynamicSettings(humanMessage)
            }
            else -> {
                if (humanMessage != "" && (humanMessage != "null" && humanMessage != "Unknown error" && humanMessage != "")) {
                    if (errorCode.isNotEmpty() && humanMessage.isNotEmpty()) {
                        showDialog.value = true
                    }
                    if (showDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showDialog.value = false },
                            title = { Text("") },
                            text = { Text(humanMessage) },
                            confirmButton = {
                                SimpleTextButton(
                                    text = "OK",
                                    backgroundColor = colors.grayLayout,
                                    onClick = {
                                        showDialog.value = false
                                        error.humanMessage = ""
                                    }
                                )
                            }
                        )
                    }
                }

                val eventParameters = mapOf(
                    "exception_code" to errorCode,
                    "exception_message" to humanMessage
                )
                analyticsHelper.reportEvent("exceptions_event", eventParameters)
            }
        }
    }
}




