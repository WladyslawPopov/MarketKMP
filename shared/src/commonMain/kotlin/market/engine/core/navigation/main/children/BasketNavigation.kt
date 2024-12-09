package market.engine.core.navigation.main.children

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import market.engine.presentation.basket.BasketContent

sealed class ChildBasket {
    class BasketChild(val component: market.engine.presentation.basket.BasketComponent) : ChildBasket()
}

@Composable
fun BasketNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildBasket>>
) {
    val stack by childStack.subscribeAsState()
    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildBasket.BasketChild -> BasketContent(screen.component)
        }
    }
}
