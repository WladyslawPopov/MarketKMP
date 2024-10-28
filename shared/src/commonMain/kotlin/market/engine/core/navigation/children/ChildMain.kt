package market.engine.core.navigation.children

sealed class ChildMain {
        data object HomeChildMain : ChildMain()
        data object CategoryChildMain : ChildMain()
        data object BasketChildMain : ChildMain()
        data object FavoritesChildMain : ChildMain()
        data object ProfileChildMain : ChildMain()
}
