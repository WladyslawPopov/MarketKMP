package market.engine.core.data.items

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.widgets.tooltip.TooltipData
import org.jetbrains.compose.resources.DrawableResource

@Serializable
data class NavigationItem(
    val title : String,
    val subtitle : String? = null,
    val imageString: String? = null,
    val hasNews : Boolean = false,
    val badgeCount : Int? = null,
    val isVisible: Boolean = true
)

data class NavigationItemUI(
    val data : NavigationItem,
    val icon : DrawableResource? = null,
    val image : DrawableResource? = null,
    val tint : Color = colors.black,
    val tintSelected : Color = colors.titleTextColor,
    val tooltipData: TooltipData? = null,
    val onClick : () -> Unit = {}
)
