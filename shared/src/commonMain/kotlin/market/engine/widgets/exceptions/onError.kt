package market.engine.widgets.exceptions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.network.ServerErrorException
import market.engine.widgets.buttons.SimpleTextButton

@Composable
fun onError(error : ServerErrorException, onRefresh: () -> Unit) {
    val humanMessage = error.humanMessage
    val errorCode = error.errorCode

    val showDialog = remember { mutableStateOf(false) }

    errorCode.let {
        when{
            (it.contains("Unable to resolve host") || it.contains("failed to connect to") || it.contains("Failed to connect")) -> {
                showNoInternetLayout(onRefresh)
            }
            it == "MISSING_OR_INVALID_TOKEN" -> {
//                globalMethods.removeShortcuts(requireContext())
//                userRepository.delete()
//                activityLauncher.launchMainActivity("", true)
//                requireActivity().finish()
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
//                (activity as BaseActivity).callResetPassword(exception.humanMessage)
            }
            else -> {
                if(humanMessage != "" && (humanMessage != "null" && humanMessage != "Unknown error" && humanMessage != "")){
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
            }
        }
    }
}




