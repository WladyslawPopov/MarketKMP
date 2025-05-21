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
import androidx.compose.ui.text.AnnotatedString
import kotlinx.serialization.json.JsonElement
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.OfferItem
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.dialogs.CustomDialog
import market.engine.widgets.dropdown_menu.PopUpMenu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun HeaderOfferBar(
    offer: OfferItem,
    isSelected: Boolean = false,
    onUpdateOfferItem : (Long) -> Unit,
    refreshPage : (() -> Unit)? = null,
    onUpdateTrigger: Int,
    baseViewModel: BaseViewModel,
    onSelectionChange: ((Boolean) -> Unit)? = null
) {
    val isOpenPopup = remember { mutableStateOf(false) }

    if(onUpdateTrigger < 0) return
    val showCreatedDialog = remember { mutableStateOf("") }
    val title = remember { mutableStateOf(AnnotatedString("")) }
    val fields = remember { mutableStateOf< ArrayList<Fields>>(arrayListOf()) }
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
                    title.value = AnnotatedString(t)
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

    val isClicked = remember { mutableStateOf(false) }

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

            PopUpMenu(
                openPopup = isOpenPopup.value,
                menuList = menuList.value,
                onClosed = { isOpenPopup.value = false }
            )

            if (showCreatedDialog.value.isNotEmpty()) {
                CustomDialog(
                    showDialog = showCreatedDialog.value != "",
                    containerColor = colors.primaryColor,
                    title = title.value,
                    body = {
                        SetUpDynamicFields(fields.value)
                    },
                    onDismiss = {
                        showCreatedDialog.value = ""
                        isClicked.value = false
                    },
                    onSuccessful = {
                        if (!isClicked.value) {
                            isClicked.value = true
                            val bodyPost = HashMap<String, JsonElement>()
                            fields.value.forEach { field ->
                                if (field.data != null) {
                                    bodyPost[field.key ?: ""] = field.data!!
                                }
                            }

                            baseViewModel.postOfferListFieldForOffer(
                                offer.id,
                                showCreatedDialog.value,
                                bodyPost,
                                onSuccess = {
                                    showCreatedDialog.value = ""
                                    isClicked.value = false
                                    onUpdateOfferItem(offer.id)
                                    refreshPage?.invoke()
                                },
                                onError = { f ->
                                    fields.value = f
                                    isClicked.value = false
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
