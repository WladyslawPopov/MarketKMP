package market.engine.navigation.main.children

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.fragments.basket.BasketComponent
import market.engine.fragments.basket.BasketContent

@Serializable
sealed class BasketConfig {
    @Serializable
    data object BasketScreen : BasketConfig()
}

sealed class ChildBasket {
    class BasketChild(val component: BasketComponent) : ChildBasket()
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


fun createBasketChild(
    config: BasketConfig,
    componentContext: ComponentContext,
    basketNavigation : StackNavigation<BasketConfig>
): ChildBasket =
    when (config) {
        BasketConfig.BasketScreen -> ChildBasket.BasketChild(
            itemBasket(componentContext, basketNavigation)
        )
    }

fun itemBasket(componentContext: ComponentContext, basketNavigation : StackNavigation<BasketConfig>): BasketComponent {
    return market.engine.fragments.basket.DefaultBasketComponent(
        componentContext = componentContext,
        navigation = basketNavigation
    )
}
