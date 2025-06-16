package market.engine.core.data.states

import androidx.compose.material.BottomSheetValue
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.ActiveWindowListingType

@Serializable
data class ListingBaseState(
    val bottomSheetState : BottomSheetValue = BottomSheetValue.Collapsed,
    val listingData : LD = LD(),
    val searchData : SD = SD(),
    val promoList: List<OfferItem>? = null,
    val isReversingPaging : Boolean = false,
    val activeWindowType: ActiveWindowListingType = ActiveWindowListingType.LISTING,
    val columns : Int = 1,
)
