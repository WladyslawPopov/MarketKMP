package market.engine.common

import androidx.compose.runtime.Composable

lateinit var viewAdditionalAuthorizationContent : @Composable (onSuccess: (HashMap<String, String>) -> Unit) -> Unit

@Composable
actual fun additionalAuthorizationContent(onSuccess: (HashMap<String, String>) -> Unit) {
    if (::viewAdditionalAuthorizationContent.isInitialized) {
        viewAdditionalAuthorizationContent{
            onSuccess(it)
        }
    }
}
