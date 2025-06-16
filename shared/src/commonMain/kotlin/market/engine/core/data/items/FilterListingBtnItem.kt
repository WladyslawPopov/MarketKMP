package market.engine.core.data.items

data class FilterListingBtnItem(
    val text : String = "",
    val removeFilter : () -> Unit = {},
    val itemClick : () -> Unit = {}
)
