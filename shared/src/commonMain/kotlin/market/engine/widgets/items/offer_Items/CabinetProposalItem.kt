package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.states.CabinetOfferItemState
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.bars.HeaderOfferBar
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.rows.UserRow
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CabinetProposalItem(
    state : CabinetOfferItemState,
    updateItem : Long? = null,
) {
    val offer = state.item
    val offerRepository = state.offerRepository
    val events = offerRepository.events

    val menuList = offerRepository.operationsList.collectAsState()
    val openMenu = remember { mutableStateOf(false) }
    val defOptions = remember { mutableStateOf<List<MenuItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        defOptions.value = offerRepository.getDefOperations()
    }

    LaunchedEffect(updateItem) {
        if (updateItem == offer.id) {
            offerRepository.update()
        }
    }

    val date1 = offer.session?.start?.convertDateWithMinutes()
    val date2 = offer.session?.end?.convertDateWithMinutes()
    val d3 = "$date1 – $date2"

    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.small,
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
            horizontalAlignment = Alignment.Start
        ) {
            HeaderOfferBar(
                offer = offer,
                selectedState = state.selectedItem,
                defOptions = defOptions.value
            )
            Row(
                modifier = Modifier.clickable {
                    events.openCabinetOffer()
                }.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageSize = 100.dp

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
                ) {
                    Box(
                        modifier = Modifier.size(imageSize),
                    ) {
                        LoadImage(
                            offer.images.firstOrNull() ?: "empty",
                            modifier = Modifier.size(imageSize)
                        )
                    }

                    Column {
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
                            openMenu.value = true
                        }

                        PopUpMenu(
                            openPopup = openMenu.value,
                            menuList = menuList.value,
                            onClosed = { openMenu.value = false }
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TitleText(offer.title, color = colors.actionTextColor)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Image(
                            painter = painterResource(drawables.locationIcon),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.smallIconSize)
                        )
                        Text(
                            offer.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.black
                        )
                    }

                    val deliveryMethods =
                        offer.deliveryMethods?.joinToString { it.name ?: "" } ?: ""
                    if (deliveryMethods.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {

                            Icon(
                                painter = painterResource(drawables.trackIcon),
                                contentDescription = "",
                                tint = colors.textA0AE,
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                            Text(
                                deliveryMethods,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Image(
                            painter = painterResource(drawables.iconClock),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.smallIconSize)
                        )
                        var date = d3
                        if (offer.session == null) {
                            date = stringResource(strings.offerSessionCompletedLabel)
                        }
                        Text(
                            date,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.black
                        )
                    }

                    UserRow(
                        offer.seller,
                        Modifier.clip(MaterialTheme.shapes.small).clickable {
                            events.goToUserPage()
                        }.padding(dimens.extraSmallPadding),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End)
                    ) {
                        Text(
                            "${stringResource(strings.priceParameterName)}: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.grayText
                        )

                        Text(
                            offer.price + " " + stringResource(strings.currencySign),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.priceTextColor
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleTextButton(
                    stringResource(strings.proposalTitle),
                    leadIcon = {
                        Icon(
                            painterResource(drawables.proposalIcon),
                            contentDescription = "",
                            tint = colors.alwaysWhite,
                            modifier = Modifier.size(dimens.extraSmallIconSize)
                        )
                    },
                    textStyle = MaterialTheme.typography.labelSmall,
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite,
                    modifier = Modifier.weight(1f)
                ) {
                    events.goToProposalPage(
                        if(offer.seller.id == UserData.login)
                         ProposalType.ACT_ON_PROPOSAL
                        else ProposalType.MAKE_PROPOSAL
                    )
                }

                SimpleTextButton(
                    stringResource(strings.writeSellerLabel),
                    leadIcon = {
                        Icon(
                            painterResource(drawables.mail),
                            contentDescription = "",
                            tint = colors.alwaysWhite,
                            modifier = Modifier.size(dimens.extraSmallIconSize)

                        )
                    },
                    textStyle = MaterialTheme.typography.labelSmall,
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite,
                    modifier = Modifier.weight(1f)
                ) {
                    offerRepository.openMesDialog()
                }
            }
        }
    }

    OfferOperationsDialogs(
        offerRepository
    )
}
