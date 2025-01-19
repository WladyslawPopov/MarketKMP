package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Order
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderDetailsDialog(
    isDialogOpen: Boolean,
    order: Order,
    onDismiss: () -> Unit,
) {
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(stringResource(strings.paymentAndDeliveryLabel))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val address = order.deliveryAddress?.address.orEmpty()
                    val city = order.deliveryAddress?.city?.jsonPrimitive?.content.orEmpty()
                    val comment = order.comment.orEmpty()
                    val dealType = order.dealType?.name.orEmpty()
                    val deliveryMethod = order.deliveryMethod?.name.orEmpty()
                    val name = order.deliveryAddress?.name.orEmpty()
                    val country = order.deliveryAddress?.country.orEmpty()
                    val paymentMethod = order.paymentMethod?.name.orEmpty()
                    val phone = order.deliveryAddress?.phone.orEmpty()
                    val index = order.deliveryAddress?.zip.orEmpty()

                    InfoRow(label = stringResource(strings.paymentMethodLabel), value = paymentMethod)
                    InfoRow(label = stringResource(strings.deliveryMethodLabel), value = deliveryMethod)
                    InfoRow(label = stringResource(strings.dealTypeLabel), value = dealType)
                    InfoRow(label = stringResource(strings.dialogFio), value = name)
                    InfoRow(label = stringResource(strings.dialogCountry), value = country)
                    InfoRow(label = stringResource(strings.dialogPhone), value = phone)
                    InfoRow(label = stringResource(strings.dialogCity), value = city)
                    InfoRow(label = stringResource(strings.dialogZip), value = index)
                    InfoRow(label = stringResource(strings.dialogAddress), value = address)
                    InfoRow(label = stringResource(strings.dialogComment), value = comment)
                }
            },
            confirmButton = {

            },
            containerColor = colors.white,
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite
                ) {
                    onDismiss()
                }
            }
        )
    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = dimens.smallPadding),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.grayText
        )
    }
}
