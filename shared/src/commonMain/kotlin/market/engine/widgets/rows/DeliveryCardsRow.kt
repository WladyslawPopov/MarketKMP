package market.engine.widgets.rows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource


@Composable
fun DeliveryCardsContent(
    selectedCards : Long,
    cards : List<DeliveryAddress>,
    setActiveCard : (DeliveryAddress) -> Unit,
    addNewCard : () -> Unit
){
    val showFields = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
    ) {

        SeparatorLabel(
            stringResource(strings.addressCardsTitle),
        )

        AnimatedVisibility(!showFields.value) {
            AcceptedPageButton(
                strings.addNewDeliveryCard,
                containerColor = colors.brightGreen
            ) {
                addNewCard()
                showFields.value = true
            }
        }

        AnimatedVisibility(!showFields.value) {
            DeliveryCardsRow(
                selectedCards,
                cards,
                setActiveCard
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleTextButton(
                stringResource(strings.actionDelete),
            ){

            }

            SimpleTextButton(
                stringResource(strings.editCardLabel),
                backgroundColor = colors.greenWaterBlue
            ){

            }

            SimpleTextButton(
                stringResource(strings.saveDataLabel),
                backgroundColor = colors.textA0AE
            ){

            }
        }

        //fields
    }
}

@Composable
fun DeliveryCardsRow(
    selectedCards : Long,
    cards : List<DeliveryAddress>,
    setActiveCard : (DeliveryAddress) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(dimens.smallPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
        verticalAlignment = Alignment.CenterVertically
    ){
        items(cards.size, key = { cards[it].id }){
            DeliveryCardItem(
                selectedCards == cards[it].id,
                cards[it],
                setActiveCard
            )
        }
    }
}

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
        onClick = {
            setActiveCard(card)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.solidGreen else colors.white,
            contentColor = colors.black
        )
    ) {
        Column(
            modifier = Modifier.padding(dimens.mediumPadding).widthIn(min = 150.dp, max = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
        ) {
            Text(
                text = "$surname $zip $city",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black
            )
            Text(
                text = address ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black
            )
            Text(
                text = phone ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black
            )
            Text(
                text = country ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black
            )
            Text(
                text = if (isDefault) stringResource(strings.defaultCardLabel) else "",
                style = MaterialTheme.typography.titleSmall,
                color = colors.black
            )
        }
    }
}
