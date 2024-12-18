package market.engine.core.data.items

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD


data class ListingData(
    var _searchData: SD = SD(),
    var _data: LD = LD(),
    var searchData : Value<SD> = MutableValue(_searchData),
    var data: Value<LD> = MutableValue(_data)
) {
    fun deepCopy(): ListingData {
        return ListingData(
            searchData = MutableValue(searchData.value.copy()),
            data = MutableValue(data.value.copy())
        )
    }
}
