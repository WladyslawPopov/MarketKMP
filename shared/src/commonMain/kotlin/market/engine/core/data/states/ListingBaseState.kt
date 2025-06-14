package market.engine.core.data.states

import androidx.compose.material.BottomSheetValue
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.OfferItem
import market.engine.fragments.root.main.listing.ActiveWindowType

data class ListingBaseState(
    val bottomSheetState : BottomSheetValue = BottomSheetValue.Collapsed,
    val listingData : LD = LD(),
    val searchData : SD = SD(),
    val promoList: List<OfferItem>? = null,
    val isReversingPaging : Boolean = false,
    val activeWindowType: ActiveWindowType = ActiveWindowType.LISTING,
    val columns : Int = 1,
)
