package market.engine.widgets.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun RichTextStyleButton(
    onClick: () -> Unit,
    icon: DrawableResource,
    tint: Color? = null,
    isSelected: Boolean = false,
) {
    IconButton(
        modifier = Modifier.padding(dimens.extraSmallPadding).background(
                color = if (isSelected) {
                    colors.rippleColor
                } else {
                    colors.transparent
                },
                shape = MaterialTheme.shapes.medium
            ).size(dimens.mediumIconSize)

            // Workaround to prevent the rich editor
            // from losing focus when clicking on the button
            // (Happens only on Desktop)
            .focusProperties { canFocus = false },
        onClick = onClick,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = "",
            tint = tint ?: colors.black,
            modifier = Modifier.size(dimens.smallIconSize)
        )
    }
}
