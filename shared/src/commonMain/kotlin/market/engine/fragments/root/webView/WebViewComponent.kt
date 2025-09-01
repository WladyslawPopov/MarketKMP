package market.engine.fragments.root.webView

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack

interface WebViewComponent {
    val url: String
    val title: String
    fun onBackClicked()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultWebViewComponent(
    componentContext: JetpackComponentContext,
    override val url: String,
    override val title: String,
) : WebViewComponent, JetpackComponentContext by componentContext {
    override fun onBackClicked() {
        goBack()
    }
}
