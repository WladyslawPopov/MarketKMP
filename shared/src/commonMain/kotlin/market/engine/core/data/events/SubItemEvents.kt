package market.engine.core.data.events

import market.engine.core.data.items.MenuItem


interface SubItemEvents {
    fun changeActiveSub()
    fun getMenuOperations(callback : (List<MenuItem>) -> Unit)
    fun onUpdateItem()
    fun onItemClick()
}
