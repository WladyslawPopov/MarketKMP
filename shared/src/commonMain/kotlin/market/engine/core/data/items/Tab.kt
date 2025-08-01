package market.engine.core.data.items

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import market.engine.core.utils.getCurrentDate
import org.jetbrains.compose.resources.DrawableResource

@Immutable
@Serializable
data class Tab(
    val title: String = "",
    val image: String? = null,
    val isPined: Boolean = false,
    val id: Long = getCurrentDate().toLong(),
)

@Immutable
data class TabWithIcon(
    val title: String = "",
    val image: String? = null,
    val isPined: Boolean = false,
    val id: Long = getCurrentDate().toLong(),
    val icon: DrawableResource? = null,
)
