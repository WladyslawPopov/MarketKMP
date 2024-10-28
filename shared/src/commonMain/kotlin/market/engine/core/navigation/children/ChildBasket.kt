package market.engine.core.navigation.children

sealed class ChildBasket {
        class BasketChild(val component: market.engine.presentation.basket.BasketComponent) : ChildBasket()
    }
