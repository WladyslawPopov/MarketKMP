package market.engine.presentation.favorites

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import market.engine.core.navigation.configs.FavoritesConfig


interface FavoritesComponent {
    fun onCloseClicked()
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    private val navigation: StackNavigation<FavoritesConfig>
) : FavoritesComponent, ComponentContext by componentContext {

    // Omitted code

    override fun onCloseClicked() {
        navigation.pop()
    }
}
