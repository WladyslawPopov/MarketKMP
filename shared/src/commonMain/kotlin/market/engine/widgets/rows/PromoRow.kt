package market.engine.widgets.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import market.engine.core.network.networkObjects.PromoOption
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromoRow(
    promoOptions: List<PromoOption>,
    showName: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
    onItemClick: (String?) -> Unit
) {
    if(showName) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SeparatorLabel(stringResource(strings.activatePromoParameterName))
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        modifier = modifier
    ) {
        promoOptions.forEach { o ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.brightPurple,
                        contentColor = colors.white
                    ),
                    shape = CircleShape,
                    modifier = Modifier.clickable {
                        onItemClick(o.id)
                    }
                ) {
                    when (o.id) {
                        "featured_in_listing" -> {
                            Text(
                                text = "TOP",
                                color = colors.white,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(dimens.extraSmallPadding)
                            )
                        }

                        "featured_on_main_page" -> {
                            Icon(
                                painterResource(drawables.homeIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.mediumIconSize),
                                tint = colors.white
                            )
                        }

                        "recommended_in_listing" -> {
                            Icon(
                                painter = painterResource(drawables.megaphoneIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.mediumIconSize),
                                tint = colors.white
                            )
                        }

                        "backlignt_in_listing" -> {
                            Icon(
                                painter = painterResource(drawables.promoHighlightIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.mediumIconSize),
                                tint = colors.white
                            )
                        }

                        "featured_in_offer" -> {
                            Icon(
                                painter = painterResource(drawables.adIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.mediumIconSize),
                                tint = colors.white
                            )
                        }
                    }
                }

                if (showName) {
                    Text(
                        o.name.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.black,
                    )
                }
            }
        }
    }
}
