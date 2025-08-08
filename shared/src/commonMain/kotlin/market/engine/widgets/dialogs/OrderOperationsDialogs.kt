package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.repositories.OrderRepository
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.rows.InfoRow
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderOperationsDialog(
    orderRepository: OrderRepository
) {
    val customDialogState = orderRepository.customDialogState.collectAsState()
    val messageTextState = orderRepository.messageText.collectAsState()
    val annotatedTitle = orderRepository.annotatedTitle
    val messageText = remember { mutableStateOf(TextFieldValue(messageTextState.value)) }
    val order = orderRepository.order
    val typeGroup = orderRepository.typeGroup
    val typeDef = DealTypeGroup.BUY

    CustomDialog(
        containerColor = colors.primaryColor,
        uiState = customDialogState.value,
        annotatedString = annotatedTitle.value,
        onDismiss = {
            orderRepository.clearDialogFields()
        },
        onSuccessful = when(customDialogState.value.typeDialog){
            "order_details","show_report_to_me", "show_my_report" -> {
                null
            }
            else -> {
                {
                    orderRepository.makeOperation(customDialogState.value.typeDialog)
                }
            }
        }
    )
    { state ->
        when(state.typeDialog){
            "show_my_report","show_report_to_me" ->{
                val feedback = if (state.typeDialog != "show_my_report") {
                    if (typeGroup == typeDef) {
                        order.feedbacks?.b2s
                    } else {
                        order.feedbacks?.s2b
                    }
                } else {
                    if (typeGroup == typeDef) {
                        order.feedbacks?.s2b
                    } else {
                        order.feedbacks?.b2s
                    }
                }

                val moodColor = when (feedback?.type) {
                    "positive" -> colors.positiveGreen
                    "neutral"  -> colors.grayText
                    "negative" -> colors.negativeRed
                    else       -> colors.grayText
                }

                val moodLabel = when (feedback?.type) {
                    "positive" -> stringResource(strings.feedbackTypePositiveLabel)
                    "neutral"  -> stringResource(strings.feedbackTypeNeutralLabel)
                    "negative" -> stringResource(strings.feedbackTypeNegativeLabel)
                    else -> ""
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
                ) {
                    Text(
                        text = moodLabel,
                        color = moodColor,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = feedback?.comment ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            "order_details" ->{
                val address = order.deliveryAddress?.address.orEmpty()
                val city = order.deliveryAddress?.city?.jsonPrimitive?.content.orEmpty()
                val dealType = order.dealType?.name.orEmpty()
                val deliveryMethod = order.deliveryMethod?.name.orEmpty()
                val name = order.deliveryAddress?.name.orEmpty()
                val country = order.deliveryAddress?.country.orEmpty()
                val paymentMethod = order.paymentMethod?.name.orEmpty()
                val phone = order.deliveryAddress?.phone.orEmpty()
                val index = order.deliveryAddress?.zip.orEmpty()

                LazyColumnWithScrollBars(
                    heightMod = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    item {
                        InfoRow(label = stringResource(strings.paymentMethodLabel), value = paymentMethod)
                        InfoRow(label = stringResource(strings.deliveryMethodLabel), value = deliveryMethod)
                        InfoRow(label = stringResource(strings.dealTypeLabel), value = dealType)
                        InfoRow(label = stringResource(strings.dialogFio), value = name)
                        InfoRow(label = stringResource(strings.dialogCountry), value = country)
                        InfoRow(label = stringResource(strings.dialogPhone), value = phone)
                        InfoRow(label = stringResource(strings.dialogCity), value = city)
                        InfoRow(label = stringResource(strings.dialogZip), value = index)
                        InfoRow(label = stringResource(strings.dialogAddress), value = address)
                    }
                }
            }
            "send_message"->{
                OutlinedTextInputField(
                    value = messageText.value,
                    onValueChange = {
                        messageText.value = it
                        orderRepository.setMessageText(it.text)
                    },
                    label = stringResource(strings.messageLabel),
                    maxSymbols = 2000,
                    singleLine = false
                )
            }
            else -> {
                if (state.fields.isNotEmpty()) {
                    SetUpDynamicFields(state.fields){
                        orderRepository.setNewField(it)
                    }
                }
            }
        }
    }
}
