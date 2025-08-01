package market.engine.core.data.states

import kotlinx.serialization.Serializable

@Serializable
data class CategoryPageState(
    val catDef : String = "",
    val catBtn : String = "",
    val enabledBtn : Boolean = true,
    val categoryWithoutCounter : Boolean = false,
)
