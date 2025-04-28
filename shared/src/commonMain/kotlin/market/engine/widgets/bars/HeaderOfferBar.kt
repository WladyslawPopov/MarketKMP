package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.ToastItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.core.data.types.ToastType
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun HeaderOfferBar(
    offer: Offer,
    isSelected: Boolean = false,
    onUpdateTrigger: Int,
    baseViewModel: BaseViewModel,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    onUpdateOfferItem : (Long) -> Unit,
    refreshPage : (() -> Unit)? = null,
    goToCreateOffer : (CreateOfferType) -> Unit,
    goToDynamicSettings : (String, Long?) -> Unit = {_, _ ->},
    goToProposals : (ProposalType) -> Unit = {},
) {
    val isOpenPopup = remember { mutableStateOf(false) }

    if(onUpdateTrigger < 0) return

    Row(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {

            if (onSelectionChange != null) {
               ThemeCheckBox(
                   isSelected = isSelected,
                   onSelectionChange = onSelectionChange,
                   modifier = Modifier.size(dimens.smallIconSize)
               )
            }

            // Favorites Icon and Count
            Icon(
                painter = painterResource(drawables.favoritesIcon),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )

            Text(
                text = offer.watchersCount.toString(),
                style = MaterialTheme.typography.bodySmall,
            )

            // Views Icon and Count
            Icon(
                painter = painterResource(drawables.eyeOpen),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )

            Text(
                text = offer.viewsCount.toString(),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Column {
            val scope = baseViewModel.viewModelScope
            val errorMes = remember { mutableStateOf("") }
            val offerOperations = baseViewModel.offerOperations
            val analyticsHelper = baseViewModel.analyticsHelper

            val showDialog = remember { mutableStateOf(false) }

            val showDeleteOfferDialog = remember { mutableStateOf(false) }
            val showActivateOfferDialog = remember { mutableStateOf(false) }
            val showActivateOfferForFutureDialog = remember { mutableStateOf(false) }
            val showCreateNoteDialog = remember { mutableStateOf("") }
            val showOffersListDialog = remember { mutableStateOf("") }
            val showCreatedDialog = remember { mutableStateOf("") }

            val choices = remember{ mutableListOf<Choices>() }
            val title = remember { mutableStateOf("") }
            val fields = remember { mutableStateOf<List<Fields>>(emptyList()) }

            val successToast = stringResource(strings.operationSuccess)
            
            val copyString = stringResource(strings.copyOfferId)
            val copiedString = stringResource(strings.idCopied)

            val defOption = listOf(
                MenuItem(
                    id = "copyId",
                    title = copyString,
                    icon = drawables.copyIcon,
                    onClick = {
                        clipBoardEvent(offer.id.toString())

                        baseViewModel.showToast(
                            successToastItem.copy(
                                message = copiedString
                            )
                        )
                    }
                ),
                MenuItem(
                    id = "share",
                    title = stringResource(strings.shareOffer),
                    icon = drawables.shareIcon,
                    onClick = {
                        offer.publicUrl?.let { openShare(it) }
                    }
                ),
                MenuItem(
                    id = "calendar",
                    title = stringResource(strings.addToCalendar),
                    icon = drawables.calendarIcon,
                    onClick = {
                        offer.publicUrl?.let { openCalendarEvent(it) }
                    }
                )
            )

            val menuList = remember {
                mutableStateOf<List<MenuItem>>(emptyList())
            }

            val cbol = stringResource(strings.createNewOffersListLabel)

            SmallIconButton(
                drawables.menuIcon,
                colors.black,
                modifierIconSize = Modifier.size(dimens.smallIconSize),
                modifier = Modifier.size(dimens.smallIconSize),
            ) {
                baseViewModel.getOfferOperations(offer.id){ listOperations ->
                    menuList.value = buildList {
                        addAll(defOption)
                        if (UserData.token != "") {
                            add(
                                MenuItem(
                                    id = "create_blank_offer_list",
                                    title = cbol,
                                    icon = drawables.addFolderIcon,
                                    onClick = {
                                        baseViewModel.getFieldsCreateBlankOfferList { t, f ->
                                            title.value = t
                                            fields.value = f
                                            showCreatedDialog.value = "create_blank_offer_list"
                                        }
                                    }
                                ),
                            )
                        }
                        addAll(listOperations.map { operation ->
                            MenuItem(
                                id = operation.id ?: "",
                                title = operation.name ?: "",
                                onClick = {
                                    when (operation.id) {
                                        "watch" -> {
                                            baseViewModel.addToFavorites(offer){ isWatchedByMe ->
                                                offer.isWatchedByMe = isWatchedByMe
                                                onUpdateOfferItem(offer.id)
                                            }
                                        }
                                        "unwatch" -> {
                                            baseViewModel.addToFavorites(offer){ isWatchedByMe ->
                                                offer.isWatchedByMe = isWatchedByMe
                                                onUpdateOfferItem(offer.id)
                                            }
                                        }
                                        "create_note","edit_note" -> {
                                            baseViewModel.getNotesField(offer.id, operation.id){ f ->
                                                title.value = operation.name.toString()
                                                fields.value = f
                                                showCreateNoteDialog.value = operation.id
                                            }
                                        }
                                        "add_to_list", "remove_from_list" -> {
                                            baseViewModel.getOfferListFieldForOffer(offer.id, operation.id){ f ->
                                                title.value = operation.name.toString()
                                                fields.value = f
                                                showOffersListDialog.value = operation.id
                                            }
                                        }
                                        "delete_note" -> {
                                            baseViewModel.deleteNote(
                                                offer.id
                                            ){
                                                val eventParam = mapOf(
                                                    "lot_id" to offer.id,
                                                    "lot_name" to offer.title,
                                                    "lot_city" to offer.freeLocation,
                                                    "lot_category" to offer.catpath.lastOrNull(),
                                                    "seller_id" to offer.sellerData?.id
                                                )

                                                analyticsHelper.reportEvent(
                                                    "delete_note",
                                                    eventParam
                                                )

                                                onUpdateOfferItem(offer.id)
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
                                                            baseViewModel.showToast(
                                                                ToastItem(
                                                                    isVisible = true,
                                                                    type = ToastType.SUCCESS,
                                                                    message = successToast
                                                                )
                                                            )

                                                            onUpdateOfferItem(offer.id)
                                                        } else {
                                                            errorMes.value =
                                                                r.humanMessage.toString()
                                                            showDialog.value = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        "activate_offer_for_future" -> {
                                            showActivateOfferForFutureDialog.value = !showActivateOfferForFutureDialog.value
                                        }
                                        "activate_offer" -> {
                                            scope.launch {
                                                val response = withContext(Dispatchers.IO) {
                                                    offerOperations.getOfferOperationsActivateOffer(
                                                        offer.id
                                                    )
                                                }
                                                withContext(Dispatchers.Main) {
                                                    val resChoice = response.success
                                                    if (resChoice != null) {
                                                        resChoice.firstOrNull()?.let { field ->
                                                            choices.clear()
                                                            title.value =
                                                                field.shortDescription.toString()
                                                            field.choices?.forEach {
                                                                choices.add(it)
                                                            }
                                                        }
                                                    }

                                                    showActivateOfferDialog.value = !showActivateOfferDialog.value
                                                }
                                            }
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
                                                            baseViewModel.showToast(
                                                                ToastItem(
                                                                    isVisible = true,
                                                                    type = ToastType.SUCCESS,
                                                                    message = successToast
                                                                )
                                                            )

                                                            onUpdateOfferItem(offer.id)
                                                        } else {
                                                            errorMes.value =
                                                                r.humanMessage.toString()
                                                            showDialog.value = true
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

                                                            baseViewModel.showToast(
                                                                ToastItem(
                                                                    isVisible = true,
                                                                    type = ToastType.SUCCESS,
                                                                    message = successToast
                                                                )
                                                            )

                                                            onUpdateOfferItem(offer.id)
                                                        } else {
                                                            errorMes.value =
                                                                r.humanMessage.toString()
                                                            showDialog.value = true
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
                                                            baseViewModel.showToast(
                                                                ToastItem(
                                                                    isVisible = true,
                                                                    type = ToastType.SUCCESS,
                                                                    message = successToast
                                                                )
                                                            )
                                                            onUpdateOfferItem(offer.id)
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
                        })
                    }
                    isOpenPopup.value = true
                }
            }
            
            PopUpMenu(
                openPopup = isOpenPopup.value,
                menuList = menuList.value,
                onClosed = { isOpenPopup.value = false }
            )

            OfferOperationsDialogs(
                offer = offer,
                showDialog = showDialog,
                showDeleteOfferDialog = showDeleteOfferDialog,
                showActivateOfferDialog = showActivateOfferDialog,
                showActivateOfferForFutureDialog = showActivateOfferForFutureDialog,
                showCreateNoteDialog = showCreateNoteDialog,
                showOffersListDialog = showOffersListDialog,
                showCreatedDialog = showCreatedDialog,
                viewModel = baseViewModel,
                errorMes = errorMes,
                title = title,
                fields = fields,
                choices = choices,
                updateItem = {
                    onUpdateOfferItem(it)
                },
                refreshPage = refreshPage
            )
        }
    }
}
