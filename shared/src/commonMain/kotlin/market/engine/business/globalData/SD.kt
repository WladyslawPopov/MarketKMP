package market.engine.business.globalData

import kotlinx.serialization.Serializable

@Serializable
data class SD(
    var userID: Long = 1L,
    var fromSearch: Boolean = false,
    var searchCategoryName: String? = null,
    var searchChoice: String? = null,
    var searchCategoryID: Long? = 1L,
    var searchString: String? = null,
    var searchParentID:Long? = 1L,
    var searchParentName: String? = null,
    var searchIsLeaf: Boolean = false,
    var searchUsersLots: String? = null,
    var searchFinished: Boolean = false
)
{
    fun clear(){
        searchString = null

        searchChoice = null

        fromSearch = false
        userID = 1L

        searchUsersLots = null
        searchFinished = false
    }

    fun clearCategory(){
        searchCategoryID = 1L
        searchCategoryName = null
        searchParentID = 1L
        searchParentName = null
        searchIsLeaf = false
    }
}
