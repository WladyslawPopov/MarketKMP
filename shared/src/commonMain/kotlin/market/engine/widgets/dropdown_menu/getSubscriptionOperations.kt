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
import kotlinx.coroutines.IO
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
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Composable
fun getSubscriptionOperations(
    subscription: Subscription,
    baseViewModel: BaseViewModel,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    onClose: () -> Unit,
) {
    val scope = baseViewModel.viewModelScope
    val subOperations : SubscriptionOperations = koinInject()
    val listItemMenu : MutableList<Operations> = remember { mutableListOf() }
    val showMenu = remember { mutableStateOf(false) }

    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

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
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val buf = subOperations.postSubOperationsEnable(
                                        subscription.id
                                    )
                                    val r = buf.success
                                    val e = buf.error
                                    withContext(Dispatchers.Main) {
                                        if (r != null) {
                                            baseViewModel.updateItem.value = subscription.id
                                            baseViewModel.showToast(
                                                successToastItem.copy(
                                                    message = operationString
                                                )
                                            )
                                            onClose()
                                        } else {
                                            if (e != null) {
                                               baseViewModel.onError(e)
                                            }
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "disable_subscription" ->{
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val buf  =
                                        subOperations.postSubOperationsDisable(
                                            subscription.id
                                        )
                                    val r = buf.success
                                    val e = buf.error
                                    withContext(Dispatchers.Main) {
                                        if (r != null) {
                                            baseViewModel.updateItem.value = subscription.id
                                            baseViewModel.showToast(
                                                successToastItem.copy(
                                                    message = operationString
                                                )
                                            )
                                            onClose()
                                        } else {
                                            if (e != null) {
                                                baseViewModel.onError(e)
                                            }
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "edit_subscription" ->{
                            baseViewModel.activeFiltersType.value = "create_new_subscription/$subscription.id"
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
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val buf = subOperations.postSubOperationsDelete(
                                    subscription.id
                                )
                                val r = buf.success
                                val e = buf.error
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        val eventParameters = mapOf(
                                            "buyer_id" to UserData.login,
                                            "item_id" to subscription.id
                                        )
                                        analyticsHelper.reportEvent(
                                            "delete_subscription",
                                            eventParameters
                                        )

                                        baseViewModel.updateItem.value = subscription.id
                                        baseViewModel.showToast(
                                            successToastItem.copy(
                                                message = operationString
                                            )
                                        )
                                        onClose()
                                    } else {
                                        if (e != null) {
                                            baseViewModel.onError(e)
                                        }
                                        onClose()
                                    }
                                }
                            }
                        }
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
