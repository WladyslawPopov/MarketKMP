package market.engine.core.items

import com.arkivanov.decompose.value.Value
import market.engine.core.globalData.LD
import market.engine.core.globalData.SD

data class ListingData(
    var searchData : Value<SD>,
    var data: Value<LD>
)
