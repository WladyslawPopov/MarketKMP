package market.engine.core.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class LD(
    var filters : ArrayList<Filter> = arrayListOf(),
    var sort : Sort? = null,
    var listingType : Int = 0,

    //pagination data
    var methodServer : String = "",
    var objServer : String = "",
    val pageCountItems: Int = 60,
    var totalCount: Int = 0,
    var totalPages: Int = totalCount / pageCountItems,

    //scroll data
    var firstVisibleItemIndex : Int = 0,
    var firstVisibleItemScrollOffset : Int = 0,
    var prevIndex : Int? = null,
) {
    fun resetScroll(){
        firstVisibleItemIndex = 0
        firstVisibleItemScrollOffset = 0
        prevIndex = null
    }

    fun clearFilters(){
        filters = arrayListOf()
        sort = null
        listingType = 0
    }

    fun clearPagingInfo(){
        methodServer = ""
        objServer = ""
        totalCount = 0
    }
}

