package market.engine.core.data.items

import androidx.compose.runtime.Immutable

@Immutable
data class SimpleAppBarData(
    val menuData: MenuData = MenuData(),
    val onBackClick: (() -> Unit)? = null,
    val listItems: List<NavigationItemUI> = emptyList(),
)
