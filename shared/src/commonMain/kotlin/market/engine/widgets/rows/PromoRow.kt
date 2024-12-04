package market.engine.widgets.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.Offer
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromoRow(
    offer: Offer,
    showName: Boolean = false,
    modifier: Modifier = Modifier.padding(dimens.smallPadding),
    onItemClick: (String?) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.SpaceAround,
        ) {

            // offer.promoOptions?.forEach { o ->
//            Row(
//                horizontalArrangement = Arrangement.Start,
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.clickable {
//                   // onItemClick(o.id)
//                }
//            ) {
//                when (o.id) {
//
            //                               "featured_in_listing" -> {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.brightPurple,
                    contentColor = colors.white
                )
            ) {
                Text(
                    text = "TOP",
                    color = colors.alwaysWhite,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(dimens.extraSmallPadding)
                )
            }
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            if (showName) {
                Text(
//                    text = o.name ?: "",
                    text = "Promo option",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(dimens.extraSmallPadding)
                )
                Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            }


//                                }

//                                "featured_on_main_page" -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.brightPurple,
                    contentColor = colors.white
                )
            ) {
                Icon(
                    painter = painterResource(drawables.homeIcon),
                    contentDescription = "",
                    tint = colors.alwaysWhite
                )
            }
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            if (showName) {
                Text(
//                    text = o.name ?: "",
                    text = "Promo option",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(dimens.extraSmallPadding)
                )
                Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            }
//                                }

//                                "recommended_in_listing" -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.brightPurple,
                    contentColor = colors.white
                )
            ) {
                Icon(
                    painter = painterResource(drawables.megaphoneIcon),
                    contentDescription = "",
                    tint = colors.alwaysWhite
                )
            }
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            if (showName) {
                Text(
//                    text = o.name ?: "",
                    text = "Promo option",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(dimens.extraSmallPadding)
                )
                Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            }
//                                }

//                                "backlignt_in_listing" -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.brightPurple,
                    contentColor = colors.white
                )
            ) {
                Icon(
                    painter = painterResource(drawables.promoHighlightIcon),
                    contentDescription = "",
                    tint = colors.alwaysWhite
                )
            }
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            if (showName) {
                Text(
//                    text = o.name ?: "",
                    text = "Promo option",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(dimens.extraSmallPadding)
                )
                Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            }
//                                }

//                                "featured_in_offer" -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.brightPurple,
                    contentColor = colors.white
                )
            ) {
                Icon(
                    painter = painterResource(drawables.adIcon),
                    contentDescription = "",
                    tint = colors.alwaysWhite
                )
            }
//                }

            if (showName) {
                Text(
//                    text = o.name ?: "",
                    text = "Promo option",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(dimens.extraSmallPadding)
                )
                Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            }
//            }
            // }
        }
    }
}
