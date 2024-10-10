package market.engine.core.items

import market.engine.core.types.TabTypeListing
import org.jetbrains.compose.resources.DrawableResource

data class Tab(
    val type: TabTypeListing,
    val title: String,
    val icon: DrawableResource? = null,
    val onClick: () -> Unit
    )
