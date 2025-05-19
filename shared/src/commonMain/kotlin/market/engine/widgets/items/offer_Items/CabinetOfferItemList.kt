package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.core.data.types.ToastType
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.convertDateWithMinutes
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.bars.HeaderOfferBar
import market.engine.widgets.buttons.PromoBuyBtn
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.ilustrations.HorizontalImageViewer
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CabinetOfferItemList(
    item : OfferItem,
    baseViewModel: BaseViewModel,
    onItemClick: () -> Unit = {},
    isSelection : Boolean = false,
    updateTrigger : Int = 0,
    goToProposal : (ProposalType) -> Unit= { _ -> },
    onUpdateOfferItem : ((Long) -> Unit)? = null,
    refreshPage : (() -> Unit)? = null,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    goToCreateOffer : (CreateOfferType) -> Unit = { _ -> },
    goToDynamicSettings : (String, Long?) -> Unit = { _, _ -> },
) {
    if (updateTrigger < 0) return

    val pagerState = rememberPagerState(
        pageCount = { item.images.size },
    )

    val isOpenPopup = remember { mutableStateOf(false) }
    val isOpenPromoPopup = remember { mutableStateOf(false) }
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
    val menuList = remember {
        mutableStateOf<List<MenuItem>>(emptyList())
    }

    Card(
        colors = if (!item.isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = MaterialTheme.shapes.small,
        onClick = {
            onItemClick()
        }
    ) {
        if (onUpdateOfferItem != null) {
            HeaderOfferBar(
                offer = item,
                isSelected = isSelection,
                onUpdateTrigger = updateTrigger,
                baseViewModel = baseViewModel,
                onSelectionChange = onSelectionChange,
                onUpdateOfferItem = onUpdateOfferItem,
                refreshPage = refreshPage
            )
        }

        Row(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            val imageSize =
                if (isBigScreen.value){
                    250.dp
                } else {
                    165.dp
                }


            Column(
                modifier = Modifier.width(imageSize).padding(dimens.smallPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                Box(
                    modifier = Modifier.size(imageSize),
                ) {
                    if (item.images.isNotEmpty()){
                        HorizontalImageViewer(
                            images = item.images,
                            pagerState = pagerState,
                        )
                    }else{
                        Image(
                            painter = painterResource(drawables.noImageOffer),
                            contentDescription = null,
                            modifier = Modifier.size(imageSize)
                        )
                    }

                    if (item.videoUrls?.isNotEmpty() == true) {
                        SmallImageButton(
                            drawables.iconYouTubeSmall,
                            modifierIconSize = Modifier.size(dimens.mediumIconSize),
                            modifier = Modifier.align(Alignment.TopStart),
                        ){

                        }
                    }

                    if (item.discount > 0) {
                        val pd = "-" + item.discount.toString() + "%"

                        DiscountBadge(pd)
                    }
                }

                SimpleTextButton(
                    text = stringResource(strings.actionsLabel),
                    textStyle = MaterialTheme.typography.labelSmall,
                    textColor = colors.actionTextColor,
                    backgroundColor = colors.grayLayout,
                    leadIcon = {
                        Icon(
                            painter = painterResource(drawables.shareMenuIcon),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.extraSmallIconSize),
                            tint = colors.actionTextColor
                        )
                    },
                ) {
                    baseViewModel.getOfferOperations(item.id) { listOperations ->
                        menuList.value = buildList {
                            addAll(listOperations.map { operation ->
                                MenuItem(
                                    id = operation.id ?: "",
                                    title = operation.name ?: "",
                                    onClick = {
                                        when (operation.id) {
                                            "watch" -> {
                                                baseViewModel.addToFavorites(item) { isWatchedByMe ->
                                                    item.isWatchedByMe = isWatchedByMe
                                                    onUpdateOfferItem?.invoke(item.id)
                                                }
                                            }

                                            "unwatch" -> {
                                                baseViewModel.addToFavorites(item) { isWatchedByMe ->
                                                    item.isWatchedByMe = isWatchedByMe
                                                    onUpdateOfferItem?.invoke(item.id)
                                                }
                                            }

                                            "create_note", "edit_note" -> {
                                                baseViewModel.getNotesField(item.id, operation.id) { f ->
                                                    title.value = operation.name.toString()
                                                    fields.value = f
                                                    showCreateNoteDialog.value = operation.id
                                                }
                                            }

                                            "add_to_list", "edit_offer_in_list","remove_from_list" -> {
                                                baseViewModel.getOfferListFieldForOffer(
                                                    item.id,
                                                    operation.id
                                                ) { f ->
                                                    title.value = operation.name.toString()
                                                    fields.value = f
                                                    showOffersListDialog.value = operation.id
                                                }
                                            }

                                            "delete_note" -> {
                                                baseViewModel.deleteNote(
                                                    item.id
                                                ) {
                                                    val eventParam = mapOf(
                                                        "lot_id" to item.id,
                                                        "lot_name" to item.title,
                                                        "lot_city" to item.location,
                                                        "lot_category" to item.catPath.lastOrNull(),
                                                        "seller_id" to item.seller.id
                                                    )

                                                    analyticsHelper.reportEvent(
                                                        "delete_note",
                                                        eventParam
                                                    )

                                                    onUpdateOfferItem?.invoke(item.id)
                                                }
                                            }

                                            "prolong_offer" -> {
                                                scope.launch(Dispatchers.IO) {
                                                    val buf =
                                                        offerOperations.postOfferOperationsProlongOffer(
                                                            item.id
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

                                                                onUpdateOfferItem?.invoke(item.id)
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
                                                showActivateOfferForFutureDialog.value =
                                                    !showActivateOfferForFutureDialog.value
                                            }

                                            "activate_offer" -> {
                                                scope.launch {
                                                    val response = withContext(Dispatchers.IO) {
                                                        offerOperations.getOfferOperationsActivateOffer(
                                                            item.id
                                                        )
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        val resChoice = response.success
                                                        resChoice?.firstOrNull()?.let { field ->
                                                            choices.clear()
                                                            title.value =
                                                                field.shortDescription.toString()
                                                            field.choices?.forEach {
                                                                choices.add(it)
                                                            }
                                                        }

                                                        showActivateOfferDialog.value =
                                                            !showActivateOfferDialog.value
                                                    }
                                                }
                                            }

                                            "set_anti_sniper" -> {
                                                scope.launch(Dispatchers.IO) {
                                                    val buf =
                                                        offerOperations.postOfferOperationsSetAntiSniper(
                                                            item.id
                                                        )
                                                    val r = buf.success
                                                    withContext(Dispatchers.Main) {
                                                        if (r != null) {
                                                            if (r.success) {
                                                                val eventParam = mapOf(
                                                                    "lot_id" to item.id,
                                                                    "lot_name" to item.title,
                                                                    "lot_city" to item.location,
                                                                    "lot_category" to item.catPath.lastOrNull(),
                                                                    "seller_id" to item.seller.id
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

                                                                onUpdateOfferItem?.invoke(item.id)
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
                                                            item.id
                                                        )
                                                    val r = buf.success
                                                    withContext(Dispatchers.Main) {
                                                        if (r != null) {
                                                            if (r.success) {

                                                                val eventParam = mapOf(
                                                                    "lot_id" to item.id,
                                                                    "lot_name" to item.title,
                                                                    "lot_city" to item.location,
                                                                    "lot_category" to item.catPath.lastOrNull(),
                                                                    "seller_id" to item.seller.id
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

                                                                onUpdateOfferItem?.invoke(item.id)
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
                                                            item.id
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
                                                                onUpdateOfferItem?.invoke(item.id)
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
                                                goToProposal(ProposalType.ACT_ON_PROPOSAL)
                                            }

                                            "make_proposal" -> {
                                                goToProposal(ProposalType.MAKE_PROPOSAL)
                                            }

                                            "cancel_all_bids" -> {
                                                goToDynamicSettings("cancel_all_bids", item.id)
                                            }

                                            "remove_bids_of_users" -> {
                                                goToDynamicSettings("remove_bids_of_users", item.id)
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
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    verticalAlignment = Alignment.Top
                ) {
                    TitleText(item.title, modifier = Modifier.weight(1f))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(drawables.locationIcon),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.extraSmallIconSize),
                    )
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                if (!item.isPrototype) {
                    var sessionEnd = stringResource(strings.offerSessionInactiveLabel)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(drawables.iconClock),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.extraSmallIconSize),
                        )

                        Text(
                            text = buildString {
                                if (item.session != null)
                                    append((item.session?.end ?: "").convertDateWithMinutes())
                                else
                                    append(sessionEnd)
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    var typeString = ""
                    var colorType = colors.titleTextColor

                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        when (item.type) {
                            "buy_now" -> {
                                typeString = stringResource(strings.buyNow)
                                colorType = colors.buyNowColor

                                Image(
                                    painter = painterResource(drawables.iconCountBoxes),
                                    contentDescription = stringResource(strings.numberOfItems),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = item.quantity.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var buyer = item.buyer?.login ?: ""
                                var color = colors.grayText

                                if (!item.isPrototype) {
                                    if (item.quantity < 2) {
                                        if (item.buyer?.login != "" && item.buyer?.login != null) {
                                            buyer = item.buyer?.login ?: ""
                                            color = colors.ratingBlue
                                        }
                                    }

                                    Text(
                                        text = buyer,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color
                                    )
                                }
                            }

                            "ordinary_auction" -> {
                                typeString = stringResource(strings.ordinaryAuction)

                                Image(
                                    painter = painterResource(drawables.iconGroup),
                                    contentDescription = stringResource(strings.numberOfBids),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = item.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText

                                if (item.bids?.isNotEmpty() == true) {
                                    bids = item.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                                    color = colors.ratingBlue
                                }

                                Text(
                                    text = bids,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }

                            "auction_with_buy_now" -> {
                                typeString = stringResource(strings.blitzAuction)
                                colorType = colors.auctionWithBuyNow

                                Image(
                                    painter = painterResource(drawables.iconGroup),
                                    contentDescription = stringResource(strings.numberOfBids),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = item.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText
                                if (item.bids?.isNotEmpty() == true) {
                                    bids = item.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                                    color = colors.ratingBlue
                                }
                                Text(
                                    text = bids,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                        }

                        if (item.safeDeal) {
                            Image(
                                painter = painterResource(drawables.safeDealIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                        }
                    }

                    if (item.seller.id == UserData.login) {
                        item.promoOptions?.let {
                            PromoRow(it, false){

                            }
                        }
                    }
                    if (UserData.login != item.seller.id) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Text(
                                text = item.seller.login ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.brightBlue,
                            )

                            if ((item.seller.rating ?: 0) > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            colors.ratingBlue,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(dimens.extraSmallPadding)
                                ) {
                                    Text(
                                        text = item.seller.rating.toString(),
                                        color = colors.alwaysWhite,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            if (item.seller.isVerified) {
                                Image(
                                    painter = painterResource(drawables.verifiedIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.smallIconSize)
                                )
                            }
                        }
                    }

                    if (item.note != null) {
                        Row(
                            modifier = Modifier.background(
                                colors.white,
                                MaterialTheme.shapes.small
                            ).padding(dimens.smallPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Icon(
                                painterResource(drawables.editNoteIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize),
                                tint = colors.black
                            )

                            Text(
                                text = item.note ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.black,
                                modifier = Modifier.weight(1f, !isBigScreen.value)
                            )
                        }
                    }

                    Text(
                        text = typeString,
                        style = MaterialTheme.typography.titleSmall,
                        color = colorType,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (UserData.login == item.seller.id && item.state == "active") {
                        val currency = stringResource(strings.currencyCode)
                        PromoBuyBtn {
                            baseViewModel.getOfferOperations(item.id, "promo") { listOperations ->
                                menuList.value = buildList {
                                    addAll(listOperations.map { operation ->
                                        MenuItem(
                                            id = operation.id ?: "",
                                            title = "${(operation.name ?: "")} (${operation.price*-1} $currency)",
                                            onClick = {
                                                baseViewModel.getPromoOperationFields(item.id, operation.id ?: "") { t, f ->
                                                    title.value = t
                                                    fields.value.clear()
                                                    fields.value.addAll(f)
                                                    showPromoDialog.value = operation.id ?: ""
                                                }
                                            }
                                        )
                                    })
                                }
                                isOpenPromoPopup.value = true
                            }
                        }
                    }
                    PopUpMenu(
                        openPopup = isOpenPromoPopup.value,
                        menuList = menuList.value,
                        onClosed = { isOpenPromoPopup.value = false }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(item.price)
                            append(" ${stringResource(strings.currencySign)}")
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.priceTextColor,
                    )
                }
            }
        }

        if (item.relistingMode != null && UserData.login == item.seller.id && onUpdateOfferItem != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(drawables.recycleIcon),
                    contentDescription = "",
                    modifier = Modifier.size(dimens.smallIconSize),
                    tint = colors.negativeRed
                )

                Spacer(modifier = Modifier.width(dimens.smallSpacer))

                Text(
                    item.relistingMode?.name ?: "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        OfferOperationsDialogs(
            offer = item,
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
                onUpdateOfferItem?.invoke(it)
            },
            refreshPage = refreshPage
        )
    }
}
