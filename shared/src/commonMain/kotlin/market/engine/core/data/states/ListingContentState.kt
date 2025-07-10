package market.engine.core.data.states

import androidx.compose.runtime.Immutable

@Immutable
data class ListingContentState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
)
