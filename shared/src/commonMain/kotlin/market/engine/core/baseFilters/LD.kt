package market.engine.core.baseFilters

import androidx.compose.material.BottomSheetValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable

@Serializable
data class LD(
    var filters : MutableList<Filter> = mutableStateListOf(),
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

    //select items and updateItem
    var selectItems : MutableList<Long> = mutableStateListOf(),
    var updateItem : MutableState<Long?> = mutableStateOf(null),

    //filters params
    val isOpenSearch : MutableState<Boolean> = mutableStateOf(false), // first open search
    val isOpenCategory : MutableState<Boolean> = mutableStateOf(true), // first open cat
    var activeFiltersType : MutableState<String> = mutableStateOf(""),
    var bottomSheetState : MutableState<BottomSheetValue> = mutableStateOf(BottomSheetValue.Collapsed),
) {
    fun resetScroll(){
        firstVisibleItemIndex = 0
        firstVisibleItemScrollOffset = 0
        prevIndex = null
    }

    fun clearFilters(){
        filters = mutableStateListOf()
        sort = null
        listingType = 0
    }

    fun clearPagingInfo(){
        methodServer = ""
        objServer = ""
        totalCount = 0
    }
}

