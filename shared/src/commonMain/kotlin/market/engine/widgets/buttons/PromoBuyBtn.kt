package market.engine.widgets.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PromoBuyBtn(
    onItemClick: () -> Unit
) {
    SimpleTextButton(
        text = stringResource(strings.promoOptionsLabel),
        textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        textColor = colors.white,
        backgroundColor = colors.brightPurple.copy(alpha = 0.6f),
        leadIcon = {
            Icon(
                painter = painterResource(drawables.megaphoneIcon),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.white
            )
        }
    ) {
        onItemClick()
    }
}
