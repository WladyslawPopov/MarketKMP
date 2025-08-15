package market.engine.core.data.items

import kotlinx.serialization.Serializable
import market.engine.core.network.networkObjects.Fields

@Serializable
data class ProposalItem(
    val userId: Long,
    val fields: List<Fields>,
)
