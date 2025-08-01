package market.engine.core.data.events

import market.engine.core.data.items.SearchHistoryItem

interface SearchEvents {
    fun onRefresh()
    fun goToListing()
    fun onDeleteHistory()
    fun onDeleteHistoryItem(id : Long)
    fun onHistoryItemClicked(item : SearchHistoryItem)
    fun editHistoryItem(item : SearchHistoryItem)
    fun openSearchCategory(value: Boolean, complete: Boolean)
    fun clearCategory()
    fun clickUser()
    fun clearUser()
    fun clickSearchFinished()
    fun onTabSelect(tab : Int)
    fun updateSearch(value : String)
    fun clearSearch()
}
