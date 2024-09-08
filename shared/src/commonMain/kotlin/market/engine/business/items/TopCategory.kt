package market.engine.business.items

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class TopCategory(
    val id: Long,
    val parentId: Long? = null,
    val name: StringResource,
    val parentName: StringResource? = null,
    val icon: DrawableResource,
)
