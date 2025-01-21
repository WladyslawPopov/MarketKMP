package market.engine.widgets.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import market.engine.common.clipBoardEvent
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun idButton(
    id: String,
) {
    Box(
        modifier = Modifier
            .background(
                colors.lightGray,
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable {
                clipBoardEvent(id)
            }.padding(dimens.smallPadding)
    ) {
        Spacer(modifier = Modifier.width(dimens.smallSpacer))
        Text(
            "id",
            textAlign = TextAlign.Center,
            color = colors.black,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(dimens.smallSpacer))
    }
}
