package market.engine.widgets.exceptions

import androidx.compose.runtime.Composable
import market.engine.business.core.ServerErrorException

@Composable
fun onError(error : ServerErrorException, onRefresh: () -> Unit) {
    val humanMessage = error.humanMessage
    val errorCode = error.errorCode

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
//                fragmentBase.pbMain.visibility = View.VISIBLE

                if(humanMessage != "" && (humanMessage != "null" && humanMessage != "Unknown error" && humanMessage != "")){
//                    fragmentBase.pbMain.visibility = View.GONE
//                    globalMethods.alert(requireContext(),exception.errorCode, exception.humanMessage)
                }
            }
        }
    }
}




