package market.engine.core.items

import org.jetbrains.compose.resources.DrawableResource

data class TopCategory(
    val id: Long,
    val parentId: Long? = null,
    val name: String,
    val parentName: String? = null,
    val icon: DrawableResource,
)
