package market.engine.core.data.items

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.colors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class NavigationItem(
    val title : StringResource,
    val subtitle : StringResource? = null,
    val string : String? = null,
    var icon : DrawableResource,
    var image : String? = null,
    val tint : Color,
    val tintSelected : Color = colors.notifyTextColor,
    val hasNews : Boolean,
    val badgeCount : Int? = null,
    val isVisible: Boolean = true,
    val onClick : () -> Unit = {}
)
