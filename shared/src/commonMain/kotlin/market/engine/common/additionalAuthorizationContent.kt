package market.engine.common

import androidx.compose.runtime.Composable

@Composable
expect fun additionalAuthorizationContent(onSuccess: (HashMap<String, String>) -> Unit)
