package market.engine.fragments.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.common.AnalyticsFactory
import market.engine.core.network.ServerErrorException
import market.engine.core.repositories.UserRepository
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.dialogs.CustomDialog
import org.koin.compose.koinInject

@Composable
fun onError(
    error : State<ServerErrorException>,
    onRefresh: () -> Unit,
) {
    val humanMessage = error.value.humanMessage
    val errorCode = error.value.errorCode

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
                goToLogin(true)
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
                goToDynamicSettings(humanMessage, null, null)
            }
            else -> {
                val richTextState = rememberRichTextState()
                if (humanMessage != "" && (humanMessage != "null" && humanMessage != "Unknown error" && humanMessage != "")) {
                    if (errorCode.isNotEmpty() && humanMessage.isNotEmpty()) {
                        showDialog.value = true
                    }

                    CustomDialog(
                        showDialog = showDialog.value,
                        title = richTextState.setHtml(humanMessage).annotatedString.text,
                        onDismiss = {
                            error.value.errorCode = ""
                            error.value.humanMessage = ""
                            showDialog.value = false
                            onRefresh()
                        }
                    )
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




