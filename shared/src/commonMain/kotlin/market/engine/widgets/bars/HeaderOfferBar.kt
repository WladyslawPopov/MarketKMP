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
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.core.data.types.ToastType
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun HeaderOfferBar(
    offer: OfferItem,
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
    val showPromoDialog = remember { mutableStateOf("") }

    val choices = remember{ mutableListOf<Choices>() }
    val title = remember { mutableStateOf("") }
    val fields = remember { mutableStateOf< ArrayList<Fields>>(arrayListOf()) }

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
        ),
        MenuItem(
            id = "create_blank_offer_list",
            title = stringResource(strings.createNewOffersListLabel),
            icon = drawables.addFolderIcon,
            onClick = {
                baseViewModel.getFieldsCreateBlankOfferList { t, f ->
                    title.value = t
                    fields.value.clear()
                    fields.value.addAll(f)
                    showCreatedDialog.value = "create_blank_offer_list"
                }
            }
        ),
    )

    val menuList = remember {
        mutableStateOf<List<MenuItem>>(emptyList())
    }

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

        SmallIconButton(
            drawables.menuIcon,
            colors.black,
            modifierIconSize = Modifier.size(dimens.smallIconSize),
            modifier = Modifier.size(dimens.smallIconSize),
        ) {
            menuList.value = buildList {
                addAll(defOption)
            }
            isOpenPopup.value = true
        }

        Column {
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
                showPromoDialog = showPromoDialog,
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
