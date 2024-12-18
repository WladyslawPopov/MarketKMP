package market.engine.widgets.rows

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.User
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSimpleRow(
    user: User,
    modifier: Modifier = Modifier
) {
    FlowRow (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = user.login ?: "",
            style = MaterialTheme.typography.titleSmall,
            color = colors.brightBlue,
            modifier = Modifier.padding(dimens.smallPadding).align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.height(dimens.smallSpacer))

        Row(
            modifier = Modifier.align(Alignment.CenterVertically),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (user.rating > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            colors.ratingBlue,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(dimens.extraSmallPadding)
                ) {
                    Text(
                        text = user.rating.toString(),
                        color = colors.alwaysWhite,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.width(dimens.smallSpacer))
            // user rating badge
            if (user.ratingBadge?.imageUrl != null) {
                LoadImage(
                    user.ratingBadge.imageUrl,
                    isShowLoading = false,
                    isShowEmpty = false,
                    size = dimens.smallIconSize
                )
                Spacer(modifier = Modifier.width(dimens.smallPadding))
            }
            Spacer(modifier = Modifier.width(dimens.smallSpacer))
            // Verified user icon
            if (user.isVerified) {
                Image(
                    painter = painterResource(drawables.verifySellersIcon),
                    contentDescription = null,
                    modifier = Modifier.size(dimens.smallIconSize)
                )
                Spacer(modifier = Modifier.width(dimens.smallPadding))
            }
        }
    }
}
