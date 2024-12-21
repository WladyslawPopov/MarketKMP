package market.engine.core.data.items

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD

data class ListingData(
    var searchData : Value<SD> = MutableValue(SD()),
    var data: Value<LD> = MutableValue(LD())
)
