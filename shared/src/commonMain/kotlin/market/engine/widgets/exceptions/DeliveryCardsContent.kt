package market.engine.widgets.exceptions

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.Fields
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
    cards : List<DeliveryAddress>,
    fields : List<Fields>,
    setDefaultCard : (DeliveryAddress) -> Unit,
    addNewCard : suspend (Long?) -> Boolean,
    deleteCard : (DeliveryAddress) -> Unit,
){
    val showFields = remember { mutableStateOf(false) }

    val selectedCards = remember { mutableStateOf(cards.find { it.isDefault }?.id) }

    val selectedCountry = remember { mutableStateOf(0) }

    val countryDef = stringResource(strings.countryDefault)

    val scope = rememberCoroutineScope()

    val setUpFields = {
        val card = cards.find { it.id == selectedCards.value }
        if (card != null) {
            fields.forEach { field ->
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
        }
    }

    LaunchedEffect(cards){
        if (cards.isNotEmpty()){
            selectedCards.value = cards.find { it.isDefault }?.id
        }
    }

    LaunchedEffect(fields){
        if (fields.isNotEmpty()){
           setUpFields()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
    ) {
        SeparatorLabel(
            stringResource(strings.addressCardsTitle),
        )

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
                    selectedCards.value = 1L
                    fields.forEach {
                        if (it.data != null){
                            it.data = null
                        }
                    }
                    showFields.value = true
                }
            }
        }

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
                            setUpFields()
                        }
                    )
                }
            }
        }

        //fields
        AnimatedVisibility(showFields.value) {
            Column {
                fields.forEach { field ->
                    when (field.widgetType) {
                        "input" -> {
                            if (field.key != "country" && field.key != "other_country") {
                                DynamicInputField(
                                    field = field,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
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
                            field = otherCountryField,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

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
                    setDefaultCard(cards.find { it.id == selectedCards.value }
                        ?: return@SimpleTextButton)
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
                    scope.launch {
                        val res = addNewCard(selectedCards.value)
                        if (res) {
                            selectedCards.value = cards.find { it.isDefault }?.id
                            showFields.value = false
                        }
                    }
                }
            }

            AnimatedVisibility(showFields.value) {
                SimpleTextButton(
                    stringResource(strings.actionCancel),
                    backgroundColor = colors.notifyTextColor,
                    textColor = colors.alwaysWhite
                ) {
                    showFields.value = false
                    if (selectedCards.value == 1L){
                        selectedCards.value = null
                    }
                    selectedCards.value = selectedCards.value ?: cards.find { it.isDefault }?.id
                    setUpFields()
                }
            }

            AnimatedVisibility(!showFields.value && selectedCards.value != null) {
                SimpleTextButton(
                    stringResource(strings.actionDelete),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite
                ) {
                    cards.find { it.id == selectedCards.value }?.let {
                        selectedCards.value = null
                        deleteCard(it)
                    }
                }
            }
        }
    }
}


