package market.engine.core.data.items

import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.colors
import org.jetbrains.compose.resources.DrawableResource

data class NavigationItem(
    val title : String,
    val subtitle : String? = null,
    var icon : DrawableResource? = null,
    var image : DrawableResource? = null,
    val imageString: String? = null,
    val tint : Color = colors.black,
    val tintSelected : Color = colors.titleTextColor,
    val hasNews : Boolean = false,
    val badgeCount : Int? = null,
    val isVisible: Boolean = true,
    val onClick : () -> Unit = {}
)
