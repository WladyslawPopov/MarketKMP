package market.engine.core.data.items

import androidx.compose.runtime.Immutable
import market.engine.core.utils.nowAsEpochSeconds
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class Tab(
    val title: String = "",
    val image: String? = null,
    val isPined: Boolean = false,
    val id: Long = nowAsEpochSeconds().toLong(),
    val icon: DrawableResource? = null,
    val onClick : () -> Unit = {}
)
