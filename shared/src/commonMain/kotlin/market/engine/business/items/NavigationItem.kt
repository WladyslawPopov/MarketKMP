package market.engine.business.items

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

data class NavigationItem(
    val title : String,
    val icon : DrawableResource,
    val tint : Color,
    val hasNews : Boolean,
    val badgeCount : Int? = null,
    val isVisible: Boolean = true
)
