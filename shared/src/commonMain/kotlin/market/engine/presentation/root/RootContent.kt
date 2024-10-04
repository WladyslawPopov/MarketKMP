package market.engine.presentation.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.presentation.login.LoginContent
import market.engine.presentation.main.MainContent

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childStack.subscribeAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Children(
            stack = childStack,
            modifier = modifier.fillMaxSize()
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.MainChild -> {
                    MainContent(instance.component)
                }

                is RootComponent.Child.LoginChild -> {
                    LoginContent(instance.component)
                }
            }
        }
    }
}
