package market.engine.core.items

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD

data class ListingData(
    var searchData : Value<SD>,
    var data: Value<LD>
) {
    fun deepCopy(): ListingData {
        return ListingData(
            searchData = MutableValue(searchData.value.copy()),
            data = MutableValue(data.value.copy())
        )
    }
}
