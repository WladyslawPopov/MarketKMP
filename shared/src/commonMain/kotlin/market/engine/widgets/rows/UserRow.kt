package market.engine.widgets.rows

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.User
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserRow(
    user: User,
    modifier: Modifier = Modifier
) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.wrapContentSize(),
            shape = CircleShape
        ) {
            LoadImage(
                url = user.avatar?.thumb?.content ?: "",
                size = 40.dp,
                isShowLoading = false,
                isShowEmpty = false
            )
        }

        Text(
            text = user.login ?: "",
            style = MaterialTheme.typography.titleSmall,
            color = colors.brightBlue,
        )

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

        // user rating badge
        if (user.ratingBadge?.imageUrl != null) {
            LoadImage(
                user.ratingBadge.imageUrl,
                isShowLoading = false,
                isShowEmpty = false,
                size = dimens.smallIconSize
            )
        }

        // Verified user icon
        if (user.isVerified) {
            Image(
                painter = painterResource(drawables.verifySellersIcon),
                contentDescription = null,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
