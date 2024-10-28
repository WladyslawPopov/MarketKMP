package market.engine.core.items

import androidx.compose.ui.graphics.Color
import market.engine.core.constants.ThemeResources.colors
import org.jetbrains.compose.resources.DrawableResource

data class NavigationItem(
    val title : String,
    val subtitle : String? = null,
    var icon : DrawableResource,
    var image : String? = null,
    val tint : Color,
    val tintSelected : Color = colors.notifyTextColor,
    val hasNews : Boolean,
    val badgeCount : Int? = null,
    val isVisible: Boolean = true,
    val onClick : () -> Unit = {}
)
