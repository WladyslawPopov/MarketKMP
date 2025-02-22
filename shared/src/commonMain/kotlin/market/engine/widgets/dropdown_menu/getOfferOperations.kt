package market.engine.widgets.dropdown_menu

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
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
import market.engine.core.network.functions.OfferOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.common.clipBoardEvent
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ToastItem
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Operations
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.core.data.types.ToastType
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.dialogs.AccessDialog
import market.engine.widgets.dialogs.CustomDialog
import market.engine.widgets.dialogs.DateDialog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Composable
fun getOfferOperations(
    offer: Offer,
    baseViewModel: BaseViewModel,
    modifier: Modifier = Modifier,
    showCopyId : Boolean = true,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    onUpdateMenuItem: (Offer) -> Unit,
    goToCreateOffer: (CreateOfferType) -> Unit,
    goToProposals: (ProposalType) -> Unit,
    goToDynamicSettings: (String, Long?) -> Unit,
    onClose: () -> Unit,
    onBack: () -> Unit = {},
) {
    val scope = baseViewModel.viewModelScope
    val errorMes = remember { mutableStateOf("") }
    val offerOperations : OfferOperations = koinInject()
    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    val showDialog = remember { mutableStateOf(false) }

    val showDeleteOfferDialog = remember { mutableStateOf(false) }
    val listItemMenu : MutableList<Operations> = remember { mutableListOf() }
    val showMenu = remember { mutableStateOf(false) }
    val showActivateOfferDialog = remember { mutableStateOf(false) }
    val showActivateOfferForFutureDialog = remember { mutableStateOf(false) }

    val choices = remember{ mutableListOf<Choices>() }
    val title = remember { mutableStateOf("") }
    val selected = remember { mutableStateOf(choices.firstOrNull()) }

    LaunchedEffect(Unit){
        scope.launch {
            val res = offerOperations.getOperationsOffer(offer.id)
            val buf = res.success?.filter {
                it.id in listOf(
                    "watch",
                    "unwatch",
                    "prolong_offer",
                    "activate_offer_for_future",
                    "activate_offer",
                    "set_anti_sniper",
                    "unset_anti_sniper",
                    "delete_offer",
                    "cancel_all_bids",
                    "remove_bids_of_users",
                    "copy_offer_without_old_photo",
                    "finalize_session",
                    "edit_offer",
                    "copy_offer",
                    "act_on_proposal",
                    "make_proposal",
                    "cancel_all_bids",
                    "remove_bids_of_users"
                )
            }
            if (buf != null) {
                listItemMenu.addAll(buf)
                showMenu.value = true
            }else{
                showMenu.value = false
            }

            if (choices.isEmpty()) {
                val response = offerOperations.getOfferOperationsActivateOffer(
                    offer.id
                )
                val resChoice = response.success
                if (resChoice != null) {
                    resChoice.firstOrNull()?.let { field ->
                        choices.clear()
                        title.value = field.shortDescription.toString()
                        field.choices?.forEach {
                            choices.add(it)
                        }
                        selected.value = choices.firstOrNull()
                    }
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
        if (showCopyId) {
            val idString = stringResource(strings.idCopied)

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(strings.copyOfferId),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )
                },
                onClick = {
                    clipBoardEvent(offer.id.toString())

                    baseViewModel.showToast(
                        ToastItem(
                            isVisible = true,
                            message = idString,
                            type = ToastType.SUCCESS
                        )
                    )
                    onClose()
                }
            )
        }

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
                    when (operation.id) {
                        "watch" -> {
                            baseViewModel.addToFavorites(offer){
                                offer.isWatchedByMe = it
                                onUpdateMenuItem(offer)
                            }
                        }
                        "unwatch" -> {
                            baseViewModel.addToFavorites(offer){
                                offer.isWatchedByMe = it
                                onUpdateMenuItem(offer)
                            }
                        }
                        "prolong_offer" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsProlongOffer(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            onUpdateMenuItem(offer)
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "activate_offer_for_future" -> {
                            showActivateOfferForFutureDialog.value = !showActivateOfferForFutureDialog.value
                        }
                        "activate_offer" -> {
                            showActivateOfferDialog.value = !showActivateOfferDialog.value
                        }
                        "set_anti_sniper" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsSetAntiSniper(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            val eventParam = mapOf(
                                                "lot_id" to offer.id,
                                                "lot_name" to offer.title,
                                                "lot_city" to offer.freeLocation,
                                                "lot_category" to offer.catpath.lastOrNull(),
                                                "seller_id" to offer.sellerData?.id
                                            )

                                            analyticsHelper.reportEvent(
                                                "set_anti_sniper",
                                                eventParam
                                            )
                                            onUpdateMenuItem(offer)
                                            onClose()
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }
                        "unset_anti_sniper" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsUnsetAntiSniper(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {

                                            val eventParam = mapOf(
                                                "lot_id" to offer.id,
                                                "lot_name" to offer.title,
                                                "lot_city" to offer.freeLocation,
                                                "lot_category" to offer.catpath.lastOrNull(),
                                                "seller_id" to offer.sellerData?.id
                                            )

                                            analyticsHelper.reportEvent(
                                                "unset_anti_sniper",
                                                eventParam
                                            )
                                            onUpdateMenuItem(offer)
                                            onClose()
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                            onClose()
                                        }
                                    }
                                }
                            }
                        }

                        "delete_offer" -> {
                            showDeleteOfferDialog.value =
                                !showDeleteOfferDialog.value
                        }
                        "finalize_session" -> {
                            scope.launch(Dispatchers.IO) {
                                val buf =
                                    offerOperations.postOfferOperationsFinalizeSession(
                                        offer.id
                                    )
                                val r = buf.success
                                withContext(Dispatchers.Main) {
                                    if (r != null) {
                                        if (r.success) {
                                            onUpdateMenuItem(offer)
                                        } else {
                                            errorMes.value =
                                                r.humanMessage.toString()
                                            showDialog.value = true
                                        }
                                    }
                                }
                            }
                        }
                        "copy_offer_without_old_photo" -> {
                            goToCreateOffer(CreateOfferType.COPY_WITHOUT_IMAGE)
                        }
                        "edit_offer" -> {
                            goToCreateOffer(CreateOfferType.EDIT)
                        }
                        "copy_offer" -> {
                            goToCreateOffer(CreateOfferType.COPY)
                        }
                        "act_on_proposal" -> {
                            goToProposals(ProposalType.ACT_ON_PROPOSAL)
                        }
                        "make_proposal" -> {
                            goToProposals(ProposalType.MAKE_PROPOSAL)
                        }
                        "cancel_all_bids" -> {
                            goToDynamicSettings("cancel_all_bids", offer.id)
                        }
                        "remove_bids_of_users" -> {
                            goToDynamicSettings("remove_bids_of_users", offer.id)
                        }
                    }
                }
            )
        }
    }

    CustomDialog(
        showDialog = showDialog.value,
        title = stringResource(strings.messageAboutError),
        body = {
            Text(errorMes.value)
        },
        onDismiss = { showDialog.value = false }
    )

    AccessDialog(
        showDialog =  showDeleteOfferDialog.value,
        title = stringResource(strings.messageAboutError),
        onDismiss = {
            showDeleteOfferDialog.value = false
            onClose()
        },
        onSuccess = {
            scope.launch {
                val buf = withContext(Dispatchers.IO) {
                    offerOperations.postOfferOperationsDeleteOffer(
                        offer.id
                    )
                }
                val r = buf.success
                withContext(Dispatchers.Main) {
                    if (r != null) {
                        if (r.success) {
                            val eventParam = mapOf(
                                "lot_id" to offer.id,
                                "lot_name" to offer.title,
                                "lot_city" to offer.freeLocation,
                                "lot_category" to offer.catpath.lastOrNull(),
                                "seller_id" to offer.sellerData?.id
                            )

                            analyticsHelper.reportEvent(
                                "delete_offer",
                                eventParameters = eventParam
                            )
                            onUpdateMenuItem(offer)
                            onBack()
                            onClose()
                        }else{
                            errorMes.value = r.humanMessage.toString()
                            showDialog.value = true
                        }
                    }
                }
            }
            showDeleteOfferDialog.value = false
        }
    )

    CustomDialog(
        showDialog = showActivateOfferDialog.value,
        title = title.value,
        body = {
            getDropdownMenu(
                selectedText = selected.value?.name.toString(),
                selectedTextDef = choices.firstOrNull()?.name.toString(),
                selects = choices.map { it.name.toString() },
                onItemClick = { type ->
                    selected.value = choices.find { it.name == type }
                },
                onClearItem = {
                    selected.value = choices.firstOrNull()
                }
            )
        },
        onDismiss = { showActivateOfferDialog.value = false },
        onSuccessful = {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val body = HashMap<String, String>()
                    body["duration"] = selected.value?.code.toString()
                    val buf = offerOperations.postOfferOperationsActivateOffer(
                        offer.id,
                        body
                    )
                    val r = buf.success
                    withContext(Dispatchers.Main) {
                        if (r != null) {
                            if (r.success) {
                                analyticsHelper.reportEvent(
                                    "activate_offer",
                                    eventParameters = mapOf(
                                        "lot_id" to offer.id,
                                        "lot_name" to offer.title.orEmpty(),
                                        "lot_city" to offer.freeLocation.orEmpty(),
                                        "lot_category" to offer.catpath.lastOrNull(),
                                        "seller_id" to offer.sellerData?.id
                                    )
                                )
                                onUpdateMenuItem(offer)
                                onClose()
                            } else {
                                errorMes.value = r.humanMessage.toString()
                                showDialog.value = true
                            }
                        }
                    }
                }
            }
            showActivateOfferDialog.value = false
        }
    )

    DateDialog(
        showDialog = showActivateOfferForFutureDialog.value,
        isSelectableDates = true,
        onDismiss = { showActivateOfferForFutureDialog.value = false },
        onSucceed = { futureTimeInSeconds ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    val body = HashMap<String, Long>()
                    body["future_time"] = futureTimeInSeconds
                    val buf =
                        offerOperations.postOfferOperationsActivateOfferForFuture(
                            offer.id,
                            body
                        )
                    val r = buf.success
                    withContext(Dispatchers.Main) {
                        if (r != null) {
                            if (r.success) {
                                analyticsHelper.reportEvent(
                                    "activate_offer_for_future",
                                    eventParameters = mapOf(
                                        "lot_id" to offer.id,
                                        "lot_name" to offer.title.orEmpty(),
                                        "lot_city" to offer.freeLocation.orEmpty(),
                                        "lot_category" to offer.catpath.lastOrNull(),
                                        "seller_id" to offer.sellerData?.id
                                    )
                                )
                                onUpdateMenuItem(offer)
                                showActivateOfferForFutureDialog.value = false
                                onClose()
                            } else {
                                errorMes.value = r.humanMessage.toString()
                                showDialog.value = true
                            }
                        }
                    }
                }
            }
        }
    )
}
