package market.engine.presentation.basket

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import market.engine.presentation.main.BasketConfig
import market.engine.presentation.main.MainComponent


interface BasketComponent {

    fun onCloseClicked()
}

class DefaultBasketComponent(
    componentContext: ComponentContext,
    private val navigation: StackNavigation<BasketConfig>
) : market.engine.presentation.basket.BasketComponent, ComponentContext by componentContext {

    override fun onCloseClicked() {
        navigation.pop()
    }
}
