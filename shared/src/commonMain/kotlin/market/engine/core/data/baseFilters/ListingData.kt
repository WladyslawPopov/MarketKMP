package market.engine.core.data.baseFilters

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class ListingData(
    var searchData : MutableState<SD> = mutableStateOf(SD()),
    var data: MutableState<LD> = mutableStateOf(LD())
)
