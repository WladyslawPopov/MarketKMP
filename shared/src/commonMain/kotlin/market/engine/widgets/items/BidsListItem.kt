package market.engine.widgets.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.network.networkObjects.Bids
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun BidsListItem(
    i: Int,
    bid: Bids,
    offer: OfferItem,
    onRebidClick: (String) -> Unit,
    goToUser: (Long) -> Unit
) {
    var isRebidShown = false
    var isYourBidShown = false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append("${i + 1}. ")
                if (bid.moverLogin != null) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = colors.brightBlue
                        )
                    ) {
                        append(bid.moverLogin)
                    }
                }else{
                    append(bid.obfuscatedMoverLogin ?: "User")
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.black,
            modifier = Modifier.clickable {
                if(bid.moverLogin != null) {
                    goToUser(bid.moverId)
                }
            }.weight(1f),
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${bid.curprice} ${stringResource(strings.currencySign)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.black,
                textAlign = TextAlign.Center
            )

            // Rebid Button logic
            if (offer.seller.id != UserData.login) {
                if (!isRebidShown && i == 0 && bid.moverId != UserData.login) {
                    isRebidShown = true
                    SimpleTextButton(
                        text = stringResource(strings.rebidLabel),
                        backgroundColor = colors.priceTextColor,
                        textColor = colors.alwaysWhite,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.heightIn(max = 35.dp),
                    ) {
                        onRebidClick(offer.minimalAcceptablePrice)
                    }
                }
            }

            if (!isYourBidShown && bid.moverId == UserData.login) {
                isYourBidShown = true
                SimpleTextButton(
                    text = stringResource(strings.yourBidLabel),
                    backgroundColor = colors.textA0AE,
                    textColor = colors.alwaysWhite,
                    textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.heightIn(max = 35.dp),
                ) {
                }
            }
        }

        Text(
            text = bid.ts?.toDoubleOrNull()?.toLong()?.convertDateWithMinutes() ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = colors.black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
