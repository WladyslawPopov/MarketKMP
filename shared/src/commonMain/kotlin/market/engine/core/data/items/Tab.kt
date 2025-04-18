package market.engine.core.data.items

import org.jetbrains.compose.resources.DrawableResource

data class Tab(
    val title: String,
    val icon: DrawableResource? = null,
    val image: String? = null,
    val onClick: () -> Unit = {}
    )
