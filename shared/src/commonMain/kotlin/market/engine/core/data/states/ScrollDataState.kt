package market.engine.core.data.states

import kotlinx.serialization.Serializable

@Serializable
data class ScrollDataState(
    val scrollItem : Int = 0,
    val offsetScrollItem : Int = 0,
)
