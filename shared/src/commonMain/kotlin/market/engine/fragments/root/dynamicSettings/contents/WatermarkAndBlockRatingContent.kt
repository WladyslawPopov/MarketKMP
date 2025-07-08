package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import org.jetbrains.compose.resources.stringResource

@Composable
fun WatermarkAndBlockRatingContent(
    isWatermark : Boolean,
    onCheckedChange : (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Text(
            stringResource(
                if (isWatermark) strings.watermarkHeaderLabel
                else strings.settingsBlockRatingHeaderLabel
            ),
            color = colors.grayText,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = if(isWatermark) UserData.userInfo?.waterMarkEnabled ?: false
            else UserData.userInfo?.blockRatingEnabled ?: false,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedBorderColor = colors.transparent,
                checkedThumbColor = colors.positiveGreen,
                checkedTrackColor = colors.transparentGrayColor,
                uncheckedBorderColor = colors.transparent,
                uncheckedThumbColor = colors.negativeRed,
                uncheckedTrackColor = colors.transparentGrayColor,
            )
        )
    }
}
