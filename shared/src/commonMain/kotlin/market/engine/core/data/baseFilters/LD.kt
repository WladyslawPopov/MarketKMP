package market.engine.core.data.baseFilters

import kotlinx.serialization.Serializable
import market.engine.core.data.constants.PAGE_SIZE

@Serializable
data class LD(
    var filters : ArrayList<Filter> = arrayListOf(),
    var sort : Sort? = null,
    var listingType : Int = 0,

    //pagination data
    var methodServer : String = "",
    var objServer : String = "",
    var totalCount: Int = 0,
    var totalPages: Int = totalCount / PAGE_SIZE,
    var prevIndex : Int? = null,
)

