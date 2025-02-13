package market.engine.fragments.root.main.proposalPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Proposals
import market.engine.core.utils.checkValidation
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.checkboxs.DynamicRadioButtons
import market.engine.widgets.rows.UserRow
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProposalItem(
    proposals: Proposals,
    type: ProposalType,
    fields: ArrayList<Fields>?,
    goToUser: (Long) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = colors.cardColors
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (proposals.proposals?.isNotEmpty() == true){

                proposals.buyerInfo?.let { user->
                    UserRow(
                        user,
                        Modifier.clickable {
                            goToUser(user.id)
                        }.fillMaxWidth()
                    )
                }

                if (fields != null) {
                    getBody(type, fields)
                }

                proposals.proposals.forEach { proposal ->

                }
            }else{
                if (type == ProposalType.MAKE_PROPOSAL) {
                    if (fields != null) {
                        getBody(type, fields)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.smallPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleTextButton(
                    stringResource(strings.actionConfirm),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite,
                ){

                }
            }
        }
    }
}

@Composable
fun getBody(
    proposalType: ProposalType,
    fields: ArrayList<Fields>
){
    val quantityTextState = remember {
        mutableStateOf(
            TextFieldValue(
                text = (fields.find { it.key == "quantity" }?.data?.jsonPrimitive?.intOrNull ?: 1).toString()
            )
        )
    }
    val priceTextState = remember {
        mutableStateOf(
            TextFieldValue(
                text = fields.find { it.key == "price" }?.data?.jsonPrimitive?.content ?: ""
            )
        )
    }
    val commentTextState = remember {
        mutableStateOf(
            TextFieldValue(
                text = fields.find { it.key == "comment" }?.data?.jsonPrimitive?.content ?: ""
            )
        )
    }

    val currency = stringResource(strings.currencyCode)
    val forLabel = stringResource(strings.forLabel)
    val counterLabel = stringResource(strings.countsSign)

    val errorState = remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.padding(dimens.mediumPadding).pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        items(fields) { field ->
            when(field.key){
                "type" -> {
                    if (proposalType != ProposalType.MAKE_PROPOSAL)
                        DynamicRadioButtons(field)
                }
                "comment" -> {
                    val maxSymbols = field.validators?.firstOrNull()?.parameters?.max

                    val labelString = buildString {
                        when{
                            field.shortDescription != null -> append(field.shortDescription)
                            field.longDescription != null -> append(field.longDescription)
                        }
                    }

                    OutlinedTextInputField(
                        value = commentTextState.value,
                        onValueChange = {
                            field.data = JsonPrimitive(it.text)
                            commentTextState.value = it
                        },
                        maxSymbols = maxSymbols,
                        label = labelString,
                        keyboardType = KeyboardType.Text,
                    )
                }
                else ->{
                    val maxSymbols = field.validators?.firstOrNull()?.parameters?.max
                    val maxNumber = field.validators?.find { it.type == "between" }?.parameters?.max

                    val labelString = buildString {
                        when{
                            field.shortDescription != null -> append(field.shortDescription)
                            field.longDescription != null -> append(field.longDescription)
                        }
                        if (maxNumber!= null){
                            append("( ${stringResource(strings.totalLabel)}")
                            append(" $maxNumber )")
                        }
                    }

                    OutlinedTextInputField(
                        value = if (field.key == "price") priceTextState.value else quantityTextState.value,
                        onValueChange = {
                            if (field.key == "price") {
                                priceTextState.value = it
                            } else {
                                quantityTextState.value = it
                            }

                            field.data = checkValidation(field, it.text)

                            val price = priceTextState.value.text.toIntOrNull() ?: 1
                            val count = quantityTextState.value.text.toIntOrNull() ?: 1

                            errorState.value = buildString {
                                append(price / count)
                                append(" ")
                                append(currency)
                                append(" ")
                                append(forLabel)
                                append(" ")
                                append(1)
                                append(" ")
                                append(counterLabel)
                            }
                        },
                        maxSymbols = maxSymbols,
                        maxNumber = maxNumber,
                        isMandatory = true,
                        label = labelString,
                        suffix = stringResource(if(field.key == "price") strings.currencyCode else strings.countsSign),
                        keyboardType = KeyboardType.Number,
                        error = if(field.key == "price") errorState.value else null
                    )
                }
            }
        }
    }
}
