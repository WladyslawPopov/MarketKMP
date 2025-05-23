package market.engine.widgets.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Subscription
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubOperationsDialogs(
    item: Subscription,
    title: MutableState<AnnotatedString>,
    showDialog: MutableState<String>,
    viewModel : BaseViewModel,
    updateItem: () -> Unit,
) {
    val isClicked = remember { mutableStateOf(false) }
    val operationString = stringResource(strings.operationSuccess)

    when (showDialog.value) {
        "delete_subscription" -> {
            AccessDialog(
                showDialog = showDialog.value != "",
                title = title.value,
                onDismiss = {
                    showDialog.value = ""
                    isClicked.value = false
                },
                onSuccess = {
                    if (!isClicked.value) {
                        isClicked.value = true
                        viewModel.postOperationFields(
                            item.id,
                            showDialog.value,
                            "subscriptions",
                            onSuccess = {
                                val eventParameters = mapOf(
                                    "buyer_id" to UserData.login,
                                    "item_id" to item.id
                                )
                                viewModel.analyticsHelper.reportEvent(
                                    "delete_subscription",
                                    eventParameters
                                )
                                viewModel.showToast(
                                    successToastItem.copy(
                                        message = operationString
                                    )
                                )
                                updateItem()
                            },
                            errorCallback = {

                            }
                        )
                    }
                }
            )
        }
        "error" -> {
            CustomDialog(
                showDialog = showDialog.value != "",
                title = buildAnnotatedString {
                    append(stringResource(strings.messageAboutError))
                },
                body = {
                    Text(title.value)
                },
                onDismiss = { showDialog.value = "" }
            )
        }

        else -> {

        }
    }
}
