package market.engine.widgets.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.BtnTypeSize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PromoBuyBtn(
    type : BtnTypeSize = BtnTypeSize.SMALL,
    onClick: () -> Unit,
) {
    SimpleTextButton(
        text = stringResource(strings.promoOptionsLabel),
        textStyle =
            when(type) {
                BtnTypeSize.BIG -> MaterialTheme.typography.labelLarge
                BtnTypeSize.MEDIUM -> MaterialTheme.typography.labelMedium
                BtnTypeSize.SMALL -> MaterialTheme.typography.labelSmall
            },
        textColor = colors.alwaysWhite,
        backgroundColor = colors.brightPurple,
        leadIcon = {
            Icon(
                painter = painterResource(drawables.megaphoneIcon),
                contentDescription = "",
                modifier = Modifier.size(
                    when(type) {
                        BtnTypeSize.BIG -> dimens.largeIconSize
                        BtnTypeSize.MEDIUM -> dimens.mediumIconSize
                        BtnTypeSize.SMALL -> dimens.smallIconSize
                    }
                ),
                tint = colors.alwaysWhite
            )
        },
        onClick = onClick
    )
}


