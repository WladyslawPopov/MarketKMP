package market.engine.widgets.dropdown_menu

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.networkObjects.Subscription
import market.engine.fragments.root.main.favPages.subscriptions.SubViewModel
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Composable
fun getSubscriptionOperations(
    subscription: Subscription,
    viewModel: SubViewModel,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    goToEditSubscription: (Long) -> Unit,
    onClose: () -> Unit,
) {
    val scope = viewModel.viewModelScope
    val subOperations : SubscriptionOperations = koinInject()
    val listItemMenu : MutableList<Operations> = remember { mutableListOf() }
    val showMenu = remember { mutableStateOf(false) }

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    val operationString = stringResource(strings.operationSuccess)

    val showDeleteOfferDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        scope.launch {
            val res = subOperations.getOperationsSubscription(subscription.id)
            withContext(Dispatchers.Main){
                val buf = res.success?.filter {
                    it.id in listOf(
                        "enable_subscription",
                        "disable_subscription",
                        "edit_subscription",
                        "delete_subscription"
                    )
                }
                if (buf != null) {
                    listItemMenu.addAll(buf)
                    showMenu.value = true
                }else{
                    showMenu.value = false
                }
            }
        }
    }

    DropdownMenu(
        modifier = modifier.widthIn(max = 350.dp).heightIn(max = 400.dp),
        expanded = showMenu.value,
        onDismissRequest = { onClose() },
        containerColor = colors.white,
        offset = offset
    ) {
        listItemMenu.forEach { operation ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = operation.name ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )
                },
                onClick = {
                    when (operation.id)  {
                        "enable_subscription" ->{
                           viewModel.enableSubscription(subscription.id){
                                subscription.isEnabled = true
                                viewModel.updateItemTrigger.value++
                           }
                           onClose()
                        }
                        "disable_subscription" ->{
                            viewModel.disableSubscription(subscription.id){
                                subscription.isEnabled = false
                                viewModel.updateItemTrigger.value++
                            }
                            onClose()
                        }
                        "edit_subscription" ->{
                            goToEditSubscription(subscription.id)
                        }
                        "delete_subscription" ->{
                            showDeleteOfferDialog.value = true
                        }
                    }
                }
            )
        }
    }

    if (showDeleteOfferDialog.value){
        AlertDialog(
            onDismissRequest = { showDeleteOfferDialog.value = false },
            title = { Text(stringResource(strings.warningDeleteSubscription)) },
            text = {  },
            containerColor = colors.white,
            tonalElevation = 0.dp,
            confirmButton = {
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.grayLayout,
                    onClick = {
                        viewModel.deleteSubscription(subscription.id){
                            val eventParameters = mapOf(
                                "buyer_id" to UserData.login,
                                "item_id" to subscription.id
                            )
                            analyticsHelper.reportEvent(
                                "delete_subscription",
                                eventParameters
                            )
                            subscription.id = 1L
                            viewModel.updateItemTrigger.value++
                            viewModel.showToast(
                                successToastItem.copy(
                                    message = operationString
                                )
                            )
                        }
                        onClose()
                        showDeleteOfferDialog.value = false
                    }
                )
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    onClick = {
                        showDeleteOfferDialog.value = false
                        onClose()
                    }
                )
            }
        )
    }
}
