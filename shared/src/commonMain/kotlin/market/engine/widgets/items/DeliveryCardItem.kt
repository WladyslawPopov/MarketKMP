package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeliveryCardItem(
    isSelected : Boolean = false,
    card : DeliveryAddress,
    setActiveCard : (DeliveryAddress) -> Unit,
) {
    val surname = card.surname
    val zip = card.zip
    val city = card.city?.jsonPrimitive?.content
    val address = card.address
    val phone = card.phone
    val country = card.country
    val isDefault = card.isDefault

    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.solidGreen else colors.white,
            contentColor = colors.black
        ),
        onClick = {
            setActiveCard(card)
        }
    ) {
        LazyColumnWithScrollBars(
            heightMod = Modifier.width(230.dp).height(250.dp).padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            item {
                Text(
                    text = surname ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black,
                    maxLines = 1
                )
            }
            item {
                Text(
                    text = country ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black,
                    maxLines = 1
                )
            }
            item {
                Text(
                    text = zip ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black,
                    maxLines = 1
                )
            }
            item {
                Text(
                    text = city ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black,
                    maxLines = 1
                )
            }
            item {
                Text(
                    text = address ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black,
                    maxLines = 1
                )
            }
            item {
                Text(
                    text = phone ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black,
                    maxLines = 1
                )
            }
            item {
                Text(
                    text = if (isDefault) stringResource(strings.defaultCardLabel) else "",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.black,
                    maxLines = 1
                )
            }
        }
    }
}
