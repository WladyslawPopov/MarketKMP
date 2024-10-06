package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.network.networkObjects.Offer
import market.engine.core.util.printLogD
import com.skydoves.landscapist.coil3.CoilImage
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.util.getImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PromoLotItem(offer: Offer, onOfferClick: (Offer) -> Unit) {

    val imageLoadFailed = remember { mutableStateOf(false) }

    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        onClick = { onOfferClick(offer) }
    ) {
        Column(
            modifier = Modifier.padding(dimens.smallPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (imageLoadFailed.value){
                getImage(offer.images?.firstOrNull()?.urls?.mid?.content ?: "")
            }else{
                CoilImage(
                    imageModel = { offer.images?.firstOrNull()?.urls?.mid?.content },
                    previewPlaceholder = painterResource(drawables.noImageOffer),
                    failure = { e->

                        imageLoadFailed.value = true
                        printLogD("Coil", e.reason?.message)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = offer.title ?: "",
                color = colors.black,
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                letterSpacing = 0.1.sp,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
            Text(
                text = offer.currentPricePerItem.toString() + stringResource(strings.currencySign),
                color = colors.titleTextColor,
                modifier = Modifier.align(Alignment.End).padding(dimens.smallPadding),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                letterSpacing = 0.1.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
