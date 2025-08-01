package market.engine.core.data.items

import org.jetbrains.compose.resources.DrawableResource

data class MenuItem(
    val id: String,
    val title: String,
    val icon: DrawableResource? = null,
    val onClick: () -> Unit,
)
