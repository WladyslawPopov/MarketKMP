package market.engine.core.navigation.children

import market.engine.presentation.favorites.FavoritesComponent

sealed class ChildFavorites {
        class FavoritesChild(val component: FavoritesComponent) : ChildFavorites()
    }
