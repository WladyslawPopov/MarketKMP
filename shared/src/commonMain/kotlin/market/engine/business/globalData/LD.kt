package market.engine.business.globalData

import application.market.agora.business.baseFilters.Filter
import application.market.agora.business.baseFilters.Sort
import kotlinx.serialization.Serializable

@Serializable
data class LD(
    var filters : ArrayList<Filter>? = null,
    var sort : Sort? = null,
    var isLeaf: Boolean = false,
    var listingPage : Boolean = false,
    val pageCountItems: Int = 30,
    var pgCount: Int = 0,
    var totalPages: Int = 0,
    var totalCount: Int = 0,
    var prevPage : Int? = null,
    var methodServer : String = ""
){
    fun clear(){
        filters = null
        sort = null
        isLeaf = false
        listingPage = false
        pgCount = 0
        totalPages = 0
        prevPage = null
        methodServer = ""
    }
}

