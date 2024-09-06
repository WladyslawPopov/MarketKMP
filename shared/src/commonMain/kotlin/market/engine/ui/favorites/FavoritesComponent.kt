package market.engine.ui.favorites

import com.arkivanov.decompose.ComponentContext


interface FavoritesComponent {

    fun onCloseClicked()
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit
) : FavoritesComponent, ComponentContext by componentContext {

    // Omitted code

    override fun onCloseClicked() {
        onBackPressed()
    }
}
