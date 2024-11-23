package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.network.networkObjects.Offer
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.stringResource

@Composable
fun PromoOfferGridItem(offer: Offer, onOfferClick: (Offer) -> Unit) {
    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        onClick = { onOfferClick(offer) }
    ) {
        Column(
            modifier = Modifier.padding(dimens.smallPadding).widthIn(max = 300.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadImage(
                url = offer.images?.firstOrNull()?.urls?.mid?.content ?: "",
                size = 200.dp
            )
            Spacer(modifier = Modifier.height(dimens.smallSpacer))
            Text(
                text = offer.title ?: "",
                color = colors.black,
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                letterSpacing = 0.1.sp,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
            Spacer(modifier = Modifier.height(dimens.smallSpacer))
            Text(
                text = offer.currentPricePerItem.toString() + stringResource(strings.currencySign),
                color = colors.titleTextColor,
                modifier = Modifier.align(Alignment.End),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                letterSpacing = 0.1.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
