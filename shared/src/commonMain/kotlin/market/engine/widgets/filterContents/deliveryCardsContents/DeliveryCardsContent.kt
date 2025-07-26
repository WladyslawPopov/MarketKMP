package market.engine.widgets.filterContents.deliveryCardsContents

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.fragments.base.EdgeToEdgeScaffold
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
    val cardsState by viewModel.deliveryCardsState.collectAsState()
    val fieldsState by viewModel.deliveryFieldsState.collectAsState()
    val showFieldsState by viewModel.showFieldsState.collectAsState()
    val selectedCardsState by viewModel.selectedCardState.collectAsState()
    val isLoading by viewModel.isShowProgress.collectAsState()

    EdgeToEdgeScaffold(
        isLoading = isLoading
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
        )
        {
            SeparatorLabel(
                stringResource(strings.addressCardsTitle),
            )
            //create new card
            AnimatedVisibility(!showFieldsState) {
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
            AnimatedVisibility(!showFieldsState) {
                LazyRowWithScrollBars {
                    items(cardsState.size, key = { cardsState[it].id }) {
                        DeliveryCardItem(
                            cardsState[it].id == selectedCardsState,
                            cardsState[it],
                            setActiveCard = { card ->
                                viewModel.setActiveCard(card)
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(showFieldsState) {
                //fields
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(if (isBigScreen.value) 2 else 1),
                    modifier = Modifier.heightIn(500.dp, 1000.dp)
                        .wrapContentHeight(),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        Alignment.CenterHorizontally
                    ),
                    verticalItemSpacing = dimens.smallPadding,
                    content = {

                        items(fieldsState) { field ->

                            when (field.widgetType) {
                                "input" -> {
                                    if (field.key != "country" && field.key != "other_country") {
                                        DynamicInputField(
                                            field = field,
                                        ) {
                                            viewModel.setNewField(it)
                                        }
                                    }
                                }
                            }

                        }

                        item {
                            Column {
                                val countryField = fieldsState.find { it.key == "country" }
                                val otherCountryField =
                                    fieldsState.find { it.key == "other_country" }
                                // country
                                if (countryField != null) {
                                    DynamicSelect(
                                        field = countryField,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        viewModel.setNewField(it)
                                    }
                                }

                                if (otherCountryField != null) {
                                    val selectedCountryState = remember(fieldsState) {
                                        fieldsState.find { it.key == "country" }?.data?.jsonPrimitive?.intOrNull ?: 0
                                    }
                                    AnimatedVisibility(visible = selectedCountryState == 1) {
                                        DynamicInputField(
                                            field = otherCountryField
                                        ) {
                                            viewModel.setNewField(it)
                                        }
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
                AnimatedVisibility(
                    !showFieldsState && selectedCardsState != null
                            && cardsState.find { it.id == selectedCardsState }?.isDefault == false
                )
                {
                    SimpleTextButton(
                        stringResource(strings.defaultCardLabel),
                        backgroundColor = colors.textA0AE,
                        textColor = colors.alwaysWhite
                    ) {
                        cardsState.find { it.id == selectedCardsState }?.let {
                            viewModel.updateDefaultCard(it) {
                                refresh()
                            }
                        }
                    }
                }

                AnimatedVisibility(!showFieldsState && selectedCardsState != null) {
                    SimpleTextButton(
                        stringResource(strings.editCardLabel),
                        backgroundColor = colors.greenWaterBlue,
                        textColor = colors.alwaysWhite
                    ) {
                        viewModel.editCard()
                    }
                }

                AnimatedVisibility(showFieldsState) {
                    SimpleTextButton(
                        stringResource(strings.saveDataLabel),
                        backgroundColor = colors.textA0AE,
                        textColor = colors.alwaysWhite,
                        enabled = !isLoading
                    ) {
                        viewModel.saveDeliveryCard(
                            selectedCardsState,
                        )
                    }
                }

                AnimatedVisibility(showFieldsState && !isLoading) {
                    SimpleTextButton(
                        stringResource(strings.actionCancel),
                        backgroundColor = colors.negativeRed,
                        textColor = colors.alwaysWhite
                    ) {
                        viewModel.closeFields()
                    }
                }

                AnimatedVisibility(!showFieldsState && selectedCardsState != null) {
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
}
