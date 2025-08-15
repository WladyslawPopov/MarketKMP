package market.engine.core.data.states

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import kotlinx.serialization.Serializable
import market.engine.core.data.types.OfferStates

@Serializable
data class OfferViewState(
    val statusList: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val columns: StaggeredGridCells = StaggeredGridCells.Fixed(1),
    val countString : String = "",
    val buyNowCounts : List<String> = emptyList(),
    val dealTypeString : String = "",
    val deliveryMethodString : String = "",
    val paymentMethodString : String = "",

    val isMyOffer: Boolean = false,
    val offerState: OfferStates = OfferStates.ACTIVE
)
