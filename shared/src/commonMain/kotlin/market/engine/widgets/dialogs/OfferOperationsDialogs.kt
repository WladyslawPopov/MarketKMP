package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.repositories.CabinetOfferRepository
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.textFields.OutlinedTextInputField
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferOperationsDialogs(
    cabinetOfferRepository: CabinetOfferRepository,
    valuesPickerState: PickerState? = null,
    offerCounts : List<String> = emptyList(),
) {
    val customDialogState by cabinetOfferRepository.customDialogState.collectAsState()
    val myMaximalBid by cabinetOfferRepository.myMaximalBid.collectAsState()
    val messageTextState by cabinetOfferRepository.messageText.collectAsState()
    val isLoading by cabinetOfferRepository.core.isShowProgress.collectAsState()
    val messageText = remember { mutableStateOf(TextFieldValue(messageTextState)) }

    CustomDialog(
        uiState = customDialogState,
        containerColor = colors.primaryColor,
        isLoading = isLoading,
        annotatedString = cabinetOfferRepository.annotatedTitle.value,
        onDismiss = {
            cabinetOfferRepository.clearDialogFields()
        },
        onSuccessful = {
            cabinetOfferRepository.makeOperations()
        },
    )
    { state->
        when (state.typeDialog)  {
            "send_message" -> {
                OutlinedTextInputField(
                    value = messageText.value,
                    onValueChange = {
                        messageText.value = it
                        cabinetOfferRepository.setMessageText(it.text)
                    },
                    label = stringResource(strings.messageLabel),
                    maxSymbols = 2000,
                    singleLine = false
                )
            }
            "activate_offer_for_future" -> {
                DateDialog(
                    showDialog = state.typeDialog != "",
                    isSelectableDates = true,
                    isLoading = isLoading,
                    onDismiss = {
                        cabinetOfferRepository.clearDialogFields()
                    },
                    onSucceed = { futureTimeInSeconds ->
                        cabinetOfferRepository.setFutureTimeInSeconds(futureTimeInSeconds.toString())
                        cabinetOfferRepository.makeOperations()
                    }
                )
            }
            "add_bid" -> {
                val aboutBid = stringResource(strings.placeBetOnTheAmount)
                val currency = stringResource(strings.currencySign)

                val text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = colors.textA0AE,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            aboutBid
                        )
                        append(": ")
                    }

                    withStyle(
                        SpanStyle(
                            color = colors.priceTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(myMaximalBid)
                        append(currency)
                    }
                }
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
            "buy_now" -> {
                if (valuesPickerState != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    )
                    {
                        SeparatorLabel(
                            stringResource(strings.chooseAmountLabel)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.mediumPadding),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ListPicker(
                                state = valuesPickerState,
                                items = offerCounts,
                                visibleItemsCount = 3,
                                modifier = Modifier.fillMaxWidth(0.5f),
                                textModifier = Modifier.padding(dimens.smallPadding),
                                textStyle = MaterialTheme.typography.titleLarge,
                                dividerColor = colors.textA0AE
                            )
                        }
                    }
                }
            }
            else -> {
                Column {
                    if (state.fields.isNotEmpty()) {
                        SetUpDynamicFields(state.fields){
                            cabinetOfferRepository.setNewField(it)
                        }
                    }
                }
            }
        }
    }
}
