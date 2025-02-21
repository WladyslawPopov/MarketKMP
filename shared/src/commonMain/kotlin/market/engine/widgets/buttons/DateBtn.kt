package market.engine.widgets.buttons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource

@Composable
fun DateBtn(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    SimpleTextButton(
        text = text,
        leadIcon = {
            Icon(
                painterResource(drawables.calendarIcon),
                "",
                tint = colors.steelBlue,
                modifier = Modifier.size(dimens.smallIconSize)
            )
            Spacer(modifier = Modifier.width(dimens.smallPadding))
        },
        textStyle = MaterialTheme.typography.labelSmall,
        textColor = colors.titleTextColor,
        modifier = modifier
    ){
        onClick()
    }
}
