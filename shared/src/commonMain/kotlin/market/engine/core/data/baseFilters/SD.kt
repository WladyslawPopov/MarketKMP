package market.engine.core.data.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class SD(
    var searchCategoryName: String? = null,
    var searchCategoryID: Long = 1L,
    var searchParentID: Long? = 1L,
    var searchParentName: String? = null,
    var searchIsLeaf: Boolean = false,

    var searchString: String = "",
    var searchFinished: Boolean = false,
    var userSearch: Boolean = false,
    var userLogin: String? = null,
    var userID: Long = 1L,

    var isRefreshing : Boolean = false
)
{
    fun clear(){
        searchString = ""
        userID = 1L
        searchFinished = false
        userSearch = false
        userLogin = null
        isRefreshing = true
    }
}
