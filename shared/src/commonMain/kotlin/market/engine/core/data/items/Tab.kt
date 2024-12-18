package market.engine.core.data.items

import market.engine.core.data.types.TabTypeListing
import org.jetbrains.compose.resources.DrawableResource

data class Tab(
    val type: TabTypeListing,
    val title: String,
    val icon: DrawableResource? = null,
    val onClick: () -> Unit
    )
