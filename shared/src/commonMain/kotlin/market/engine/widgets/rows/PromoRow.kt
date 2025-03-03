package market.engine.widgets.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromoRow(
    offer: Offer,
    showName: Boolean = false,
    modifier: Modifier = Modifier.padding(dimens.smallPadding),
    onItemClick: (String?) -> Unit
) {
    if(offer.promoOptions != null ) {
        if(showName) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeparatorLabel(stringResource(strings.activatePromoParameterName))
            }
        }
        Box(
            modifier = modifier
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = modifier
            ) {
                offer.promoOptions.forEach { o ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = colors.brightPurple,
                            contentColor = colors.white
                        ),
                        modifier = Modifier.clickable {
                            onItemClick(o.id)
                        }
                    ) {
                        when (o.id) {
                            "featured_in_listing" -> {
                                Text(
                                    text = "TOP",
                                    color = colors.brightPurple,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(dimens.extraSmallPadding)
                                )
                            }

                            "featured_on_main_page" -> {
                                Icon(
                                    painter = painterResource(drawables.homeIcon),
                                    contentDescription = "",
                                    tint = colors.brightPurple,
                                    modifier = Modifier.size(dimens.mediumIconSize)
                                )
                            }

                           "recommended_in_listing" -> {
                                Icon(
                                    painter = painterResource(drawables.megaphoneIcon),
                                    contentDescription = "",
                                    tint = colors.brightPurple,
                                    modifier = Modifier.size(dimens.mediumIconSize)
                                )
                            }

                            "backlignt_in_listing" -> {
                                Icon(
                                    painter = painterResource(drawables.promoHighlightIcon),
                                    contentDescription = "",
                                    tint = colors.brightPurple,
                                    modifier = Modifier.size(dimens.mediumIconSize)
                                )
                            }

                            "featured_in_offer" -> {
                                Icon(
                                    painter = painterResource(drawables.adIcon),
                                    contentDescription = "",
                                    tint = colors.brightPurple,
                                    modifier = Modifier.size(dimens.mediumIconSize)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
                    if (showName) {
                        Text(
                             "Promo",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(dimens.extraSmallPadding)
                        )
                        Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
                    }
                }
            }
        }
    }
}
