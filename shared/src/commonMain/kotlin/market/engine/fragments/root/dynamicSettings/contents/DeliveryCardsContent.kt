package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.items.DeliveryCardItem
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeliveryCardsContent(
    cards: List<DeliveryAddress>,
    fields: MutableState<List<Fields>>,
    viewModel: BaseViewModel,
    refresh: () -> Unit
){
    val showFields = remember { mutableStateOf(false) }
    
    val selectedCards = remember { mutableStateOf(cards.find { it.isDefault }?.id) }

    val selectedCountry = remember { mutableStateOf(0) }

    val countryDef = stringResource(strings.countryDefault)

    val setUpFields = {
        val card = cards.find { it.id == selectedCards.value }
        if (card != null) {
            fields.value.forEach { field ->
                when (field.key) {
                    "zip" -> {
                        field.data = JsonPrimitive(card.zip)
                    }

                    "city" -> {
                        field.data = card.city
                    }

                    "address" -> {
                        field.data = JsonPrimitive(card.address)
                    }

                    "phone" -> {
                        field.data = JsonPrimitive(card.phone)
                    }

                    "surname" -> {
                        field.data = JsonPrimitive(card.surname)
                    }

                    "other_country" -> {
                        field.data = JsonPrimitive(card.country)
                    }

                    "country" -> {
                        selectedCountry.value = if (card.country == countryDef) 0 else 1
                        field.data =
                            JsonPrimitive(if (card.country == countryDef) 0 else 1)
                    }
                }
                field.errors = null
            }
        } else {
            fields.value.forEach { field ->
                field.data = null
                field.errors = null
            }
        }
    }

    LaunchedEffect(cards){
        if (cards.isNotEmpty() && selectedCards.value == null){
            selectedCards.value = cards.find { it.isDefault }?.id
        }
    }

    LaunchedEffect(selectedCards){
        setUpFields()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
    ) {
        SeparatorLabel(
            stringResource(strings.addressCardsTitle),
        )
        //create new card
        AnimatedVisibility(!showFields.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AcceptedPageButton(
                    strings.addNewDeliveryCard,
                    containerColor = colors.brightGreen,
                ) {
                    selectedCards.value = null
                    showFields.value = true
                }
            }
        }

        //card items
        AnimatedVisibility(!showFields.value) {
            LazyRow(
                modifier = Modifier.padding(dimens.smallPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
                verticalAlignment = Alignment.CenterVertically
            ){
                items(cards.size, key = { cards[it].id }){
                    DeliveryCardItem(
                        selectedCards.value == cards[it].id,
                        cards[it],
                        setActiveCard = { card ->
                            selectedCards.value = card.id
                        }
                    )
                }
            }
        }

        //fields
        AnimatedVisibility(showFields.value) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                fields.value.forEach { field ->
                    when (field.widgetType) {
                        "input" -> {
                            if (field.key != "country" && field.key != "other_country") {
                                DynamicInputField(
                                    field = field,
                                )
                            }
                        }
                    }
                }

                val countryField = fields.value.find { it.key == "country" }
                val otherCountryField = fields.value.find { it.key == "other_country" }

                // country
                if (countryField != null) {

                    selectedCountry.value = fields.value.find {
                        it.key == "country"
                    }?.data?.jsonPrimitive?.intOrNull ?: 0

                    DynamicSelect(
                        field = countryField,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) { choice ->
                        selectedCountry.value = choice?.code?.intOrNull ?: 0
                    }
                }

                if (otherCountryField != null) {
                    AnimatedVisibility(visible = selectedCountry.value == 1) {
                        DynamicInputField(
                            field = otherCountryField
                        )
                    }
                }
            }
        }
        //buttons
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        ) {
            AnimatedVisibility(!showFields.value && selectedCards.value != null && cards.find { it.id == selectedCards.value }?.isDefault == false) {
                SimpleTextButton(
                    stringResource(strings.defaultCardLabel),
                    backgroundColor = colors.textA0AE,
                    textColor = colors.alwaysWhite
                ) {
                    cards.find { it.id == selectedCards.value }?.let {
                        viewModel.updateDefaultCard(it){
                            refresh()
                        }
                    }
                }
            }

            AnimatedVisibility(!showFields.value && selectedCards.value != null) {
                SimpleTextButton(
                    stringResource(strings.editCardLabel),
                    backgroundColor = colors.greenWaterBlue,
                    textColor = colors.alwaysWhite
                ) {
                    showFields.value = true
                }
            }

            AnimatedVisibility(showFields.value) {
                SimpleTextButton(
                    stringResource(strings.saveDataLabel),
                    backgroundColor = colors.textA0AE,
                    textColor = colors.alwaysWhite
                ) {
                    viewModel.saveDeliveryCard(
                        fields.value,
                        selectedCards.value,
                        onSaved = {
                            showFields.value = false
                            refresh()
                        },
                        onError = {
                            fields.value = it
                        }
                    )
                }
            }

            AnimatedVisibility(showFields.value) {
                SimpleTextButton(
                    stringResource(strings.actionCancel),
                    backgroundColor = colors.notifyTextColor,
                    textColor = colors.alwaysWhite
                ) {
                    showFields.value = false
                    selectedCards.value = cards.find { it.isDefault }?.id
                }
            }

            AnimatedVisibility(!showFields.value && selectedCards.value != null) {
                SimpleTextButton(
                    stringResource(strings.actionDelete),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite
                ) {
                    cards.find { it.id == selectedCards.value }?.let {
                        viewModel.updateDeleteCard(
                            it
                        ){
                            selectedCards.value = null
                            refresh()
                        }
                    }
                }
            }
        }
    }
}
