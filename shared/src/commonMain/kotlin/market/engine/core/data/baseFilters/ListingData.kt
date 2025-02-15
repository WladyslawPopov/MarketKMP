package market.engine.core.data.baseFilters

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

data class ListingData(
    var searchData : Value<SD> = MutableValue(SD()),
    var data: Value<LD> = MutableValue(LD())
)
