package market.engine.core.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

interface Dimens {
    val zero: Dp

    val extraSmallPadding: Dp
    val smallPadding: Dp
    val mediumPadding: Dp
    val largePadding: Dp
    val extraLargePadding: Dp

    val extraSmallIconSize: Dp
    val smallIconSize: Dp
    val mediumIconSize: Dp
    val largeIconSize: Dp
    val extraLargeIconSize: Dp

    val smallCornerRadius: Dp
    val mediumCornerRadius: Dp
    val largeCornerRadius: Dp
    val extraLargeCornerRadius: Dp

    val smallElevation: Dp
    val mediumElevation: Dp
    val largeElevation: Dp
    val extraLargeElevation: Dp

    val smallSpacer : Dp
    val mediumSpacer : Dp
    val largeSpacer : Dp
    val extraLargeSpacer : Dp

    val smallText : TextUnit
    val mediumText : TextUnit
    val largeText : TextUnit
    val extraLargeText : TextUnit

    val bottomBar : Dp
    val appBar : Dp
}
