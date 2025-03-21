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
    fields: List<Fields>,
    viewModel: BaseViewModel,
    setUpNewFields: (List<Fields>) -> Unit,
    onError: (List<Fields>) -> Unit,
    refresh: () -> Unit
){
    val showFields = remember { mutableStateOf(cards.find { it.isDefault }?.address == null) }
    
    val selectedCards = remember { mutableStateOf(cards.find { it.isDefault }?.id) }

    val selectedCountry = remember {
        mutableStateOf(
            fields.find { it.key == "country" }?.data?.jsonPrimitive?.intOrNull ?: 0
        )
    }

    val countryDef = stringResource(strings.countryDefault)

    val setFields : (Long?) -> Unit = { selectedId ->
        val card = cards.find { it.id == selectedId }
        if (card != null) {
            fields.forEach { field ->
                when (field.key) {
                    "zip" -> {
                        field.data = JsonPrimitive(card.zip ?: "")
                    }

                    "city" -> {
                        field.data = card.city
                    }

                    "address" -> {
                        field.data = JsonPrimitive(card.address ?: "")
                    }

                    "phone" -> {
                        field.data = JsonPrimitive(card.phone ?: "")
                    }

                    "surname" -> {
                        field.data = JsonPrimitive(card.surname ?: "")
                    }

                    "other_country" -> {
                        field.data = JsonPrimitive(card.country ?: "")
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
            fields.forEach {
                it.data = null
                it.errors = null
            }
        }
        setUpNewFields(fields)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
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
                    setFields(null)
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
                        cards[it].id == selectedCards.value,
                        cards[it],
                        setActiveCard = { card ->
                            setFields(card.id)
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
                fields.forEach { field ->
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

                val countryField = fields.find { it.key == "country" }
                val otherCountryField = fields.find { it.key == "other_country" }

                // country
                if (countryField != null) {

                    selectedCountry.value = fields.find {
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
                    setFields(selectedCards.value)
                    showFields.value = true
                }
            }

            AnimatedVisibility(showFields.value) {
                SimpleTextButton(
                    stringResource(strings.saveDataLabel),
                    backgroundColor = colors.textA0AE,
                    textColor = colors.alwaysWhite,
                    enabled = !viewModel.isShowProgress.value
                ) {
                    viewModel.setLoading(true)
                    viewModel.saveDeliveryCard(
                        fields,
                        selectedCards.value,
                        onSaved = {
                            showFields.value = false
                            refresh()
                            selectedCards.value = cards.find { it.isDefault }?.id
                        },
                        onError = {
                            onError(it)
                        }
                    )
                }
            }

            AnimatedVisibility(showFields.value) {
                SimpleTextButton(
                    stringResource(strings.actionCancel),
                    backgroundColor = colors.negativeRed,
                    textColor = colors.alwaysWhite
                ) {
                    showFields.value = false
                    setFields(cards.find { it.isDefault }?.id)
                    selectedCards.value = cards.find { it.isDefault }?.id
                }
            }

            AnimatedVisibility(!showFields.value && selectedCards.value != null) {
                SimpleTextButton(
                    stringResource(strings.actionDelete),
                    backgroundColor = colors.negativeRed,
                    textColor = colors.alwaysWhite
                ) {
                    cards.find { it.id == selectedCards.value }?.let { address ->
                        viewModel.updateDeleteCard(
                            address
                        ){
                            showFields.value = false
                            selectedCards.value = null
                            refresh()
                        }
                    }
                }
            }
        }
    }
}
