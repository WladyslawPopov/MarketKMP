package market.engine.core.data.items

import androidx.compose.runtime.Immutable
import market.engine.core.utils.getCurrentDate
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class Tab(
    val title: String,
    val icon: DrawableResource? = null,
    val image: String? = null,
    val isPined: Boolean = false,
    val id: Long = getCurrentDate().toLong(),
    val onClick: () -> Unit = {},
    val onLongClick: () -> Unit = {},
)
