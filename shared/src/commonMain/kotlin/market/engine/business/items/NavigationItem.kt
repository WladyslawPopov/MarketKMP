package market.engine.business.items

import androidx.compose.ui.graphics.Color
import market.engine.business.constants.ThemeResources.colors
import org.jetbrains.compose.resources.DrawableResource

data class NavigationItem(
    val title : String,
    val subtitle : String? = null,
    val icon : DrawableResource,
    val tint : Color,
    val tintSelected : Color = colors.notifyTextColor,
    val hasNews : Boolean,
    val badgeCount : Int? = null,
    val isVisible: Boolean = true,
)
