package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.intOrNull
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.fragments.root.dynamicSettings.DeliveryCardsViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.items.DeliveryCardItem
import market.engine.widgets.rows.LazyRowWithScrollBars
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeliveryCardsContent(
    viewModel: DeliveryCardsViewModel,
    refresh: () -> Unit
){
    val uiState = viewModel.deliveryCardsState.collectAsState()
    val cards = uiState.value.deliveryCards
    val fields = uiState.value.deliveryFields
    val showFields = uiState.value.showFields
    val selectedCards = uiState.value.selectedCard
    val selectedCountry = uiState.value.selectedCountry

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
    ) {
        SeparatorLabel(
            stringResource(strings.addressCardsTitle),
        )
        //create new card
        AnimatedVisibility(!showFields) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AcceptedPageButton(
                    stringResource(strings.addNewDeliveryCard),
                    containerColor = colors.brightGreen,
                ) {
                   viewModel.addNewDeliveryCard()
                }
            }
        }

        //card items
        AnimatedVisibility(!showFields) {
            LazyRowWithScrollBars {
                items(cards.size, key = { cards[it].id }){
                    DeliveryCardItem(
                        cards[it].id == selectedCards,
                        cards[it],
                        setActiveCard = { card ->
                            viewModel.setActiveCard(card)
                        }
                    )
                }
            }
        }

        //fields
        AnimatedVisibility(showFields) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(if (isBigScreen.value) 2 else 1),
                modifier = Modifier.heightIn(200.dp, 5000.dp)
                    .wrapContentHeight(),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(
                    dimens.smallPadding,
                    Alignment.CenterHorizontally
                ),
                verticalItemSpacing = dimens.smallPadding,
                content = {
                    items(fields) { field ->
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

                    item {
                        Column {
                            val countryField = fields.find { it.key == "country" }
                            val otherCountryField = fields.find { it.key == "other_country" }
                            // country
                            if (countryField != null) {
                                DynamicSelect(
                                    field = countryField,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) { choice ->
                                    viewModel.selectedCountry(choice?.code?.intOrNull ?: 0)
                                }
                            }

                            if (otherCountryField != null) {
                                AnimatedVisibility(visible = selectedCountry == 1) {
                                    DynamicInputField(
                                        field = otherCountryField
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        //buttons
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        )
        {
            AnimatedVisibility(!showFields && selectedCards != null && cards.find { it.id == selectedCards }?.isDefault == false) {
                SimpleTextButton(
                    stringResource(strings.defaultCardLabel),
                    backgroundColor = colors.textA0AE,
                    textColor = colors.alwaysWhite
                ) {
                    cards.find { it.id == selectedCards }?.let {
                        viewModel.updateDefaultCard(it){
                            refresh()
                        }
                    }
                }
            }

            AnimatedVisibility(!showFields && selectedCards != null) {
                SimpleTextButton(
                    stringResource(strings.editCardLabel),
                    backgroundColor = colors.greenWaterBlue,
                    textColor = colors.alwaysWhite
                ) {
                   viewModel.editCard()
                }
            }

            AnimatedVisibility(showFields) {
                SimpleTextButton(
                    stringResource(strings.saveDataLabel),
                    backgroundColor = colors.textA0AE,
                    textColor = colors.alwaysWhite,
                    enabled = !viewModel.isShowProgress.value
                ) {
                    viewModel.saveDeliveryCard(
                        selectedCards,
                    )
                }
            }

            AnimatedVisibility(showFields) {
                SimpleTextButton(
                    stringResource(strings.actionCancel),
                    backgroundColor = colors.negativeRed,
                    textColor = colors.alwaysWhite
                ) {
                    viewModel.closeFields()
                }
            }

            AnimatedVisibility(!showFields && selectedCards != null) {
                SimpleTextButton(
                    stringResource(strings.actionDelete),
                    backgroundColor = colors.negativeRed,
                    textColor = colors.alwaysWhite
                ) {
                    viewModel.deleteCard()
                }
            }
        }
    }
}
