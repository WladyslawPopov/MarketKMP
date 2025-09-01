package market.engine.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import market.engine.core.data.globalData.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3Api::class)
@Composable
actual fun WebView(
    modifier: Modifier,
    url: String,
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(drawables.arrowBackIcon),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        UIKitView(
            modifier = modifier.padding(paddingValues),
            factory = {
                val webView = WKWebView(
                    frame = CGRectZero.readValue(),
                    configuration = WKWebViewConfiguration()
                )
                webView
            },
            update = { webView ->
                val nsUrl = NSURL(string = url)
                val request = NSURLRequest(uRL = nsUrl)
                webView.loadRequest(request)
            }
        )
    }
}
