package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Order
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderDetailsDialog(
    isDialogOpen: Boolean,
    order: Order,
    updateTrigger: Int,
    onDismiss: () -> Unit,
) {
    if (updateTrigger < 0) return

    val address = remember { order.deliveryAddress?.address.orEmpty() }
    val city = remember { order.deliveryAddress?.city?.jsonPrimitive?.content.orEmpty() }
    val dealType = remember { order.dealType?.name.orEmpty() }
    val deliveryMethod = remember { order.deliveryMethod?.name.orEmpty() }
    val name = remember { order.deliveryAddress?.name.orEmpty() }
    val country = remember { order.deliveryAddress?.country.orEmpty() }
    val paymentMethod = remember { order.paymentMethod?.name.orEmpty() }
    val phone = remember { order.deliveryAddress?.phone.orEmpty() }
    val index = remember { order.deliveryAddress?.zip.orEmpty() }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(stringResource(strings.paymentAndDeliveryLabel))
            },
            text = {
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
