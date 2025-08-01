package market.engine.core.data.items

data class MenuData(
    val isMenuVisible: Boolean = false,
    val menuItems: List<MenuItem> = emptyList(),
    val closeMenu: (() -> Unit) = {},
)
