package market.engine.core.data.items

import androidx.compose.runtime.Immutable
import market.engine.core.utils.getCurrentDate
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class Tab(
    val title: String = "",
    val image: String? = null,
    val isPined: Boolean = false,
    val id: Long = getCurrentDate().toLong(),
    val icon: DrawableResource? = null,
    val onClick : () -> Unit = {}
)
