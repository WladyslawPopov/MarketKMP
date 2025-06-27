package market.engine.fragments.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.core.repositories.UserRepository
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.dialogs.CustomDialog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun onError(
    error : ServerErrorException,
    onRefresh: () -> Unit,
) {
    val humanMessage = remember {  mutableStateOf(error.humanMessage) }
    val errorCode = remember {  mutableStateOf(error.errorCode) }

    val analyticsHelper = remember { AnalyticsFactory.getAnalyticsHelper() }
    val userRepository : UserRepository = koinInject()

    val showDialog = remember { mutableStateOf(false) }

    errorCode.value.let {
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
                goToDynamicSettings(humanMessage.value, null, null)
            }
            else -> {
                val richTextState = rememberRichTextState()
                if (humanMessage.value != "" && (humanMessage.value != "null" && humanMessage.value != "Unknown error" && humanMessage.value != "")) {
                    if (errorCode.value.isNotEmpty() && humanMessage.value.isNotEmpty()) {
                        showDialog.value = true
                        
                        when(humanMessage.value){
                            "EXCEEDED_LIMIT_OFFERS_LIST" ->{
                                humanMessage.value = stringResource(strings.errorExceededLimitOffersList)
                            }
                             "TITLE_IN_OFFERS_LIST_ALREADY_USE" ->{
                                humanMessage.value = stringResource(strings.errorTitleInOffersListAlreadyUse)
                            }
                        }
                    }

//                    CustomDialog(
//                        showDialog = showDialog.value,
//                        title = richTextState.setHtml(humanMessage.value).annotatedString,
//                        onDismiss = {
//                            error.errorCode = ""
//                            error.humanMessage = ""
//                            humanMessage.value = ""
//                            errorCode.value = ""
//                            showDialog.value = false
//                            onRefresh()
//                        }
//                    )
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




