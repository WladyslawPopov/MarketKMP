package market.engine.ui.basket

import com.arkivanov.decompose.ComponentContext


interface BasketComponent {

    fun onCloseClicked()
}

class DefaultBasketComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit
) : BasketComponent, ComponentContext by componentContext {

    // Omitted code

    override fun onCloseClicked() {
        onBackPressed()
    }
}
