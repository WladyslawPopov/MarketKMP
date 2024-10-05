package market.engine.core.globalData

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import market.engine.core.types.CategoryScreenType

@Serializable
data class SD(
    var userID: Long = 1L,
    var fromSearch: Boolean = false,
    var searchChoice: String? = null,
    var searchCategoryName: String? = null,
    var searchCategoryID: Long? = 1L,
    var searchString: String? = null,
    var searchParentID:Long? = 1L,
    var searchParentName: String? = null,
    var searchIsLeaf: Boolean = false,
    var searchUsersLots: String? = null,
    var searchFinished: Boolean = false,
    var categoryStack: ArrayList<CategoryScreenType> = arrayListOf(CategoryScreenType.CATEGORY),
)
{
    fun clear(){
        searchString = null

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
