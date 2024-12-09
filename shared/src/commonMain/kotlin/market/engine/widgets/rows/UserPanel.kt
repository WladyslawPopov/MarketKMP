package market.engine.widgets.rows

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.globalData.SAPI
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.network.networkObjects.User
import market.engine.core.util.convertDateWithMinutes
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserPanel(
    modifier: Modifier,
    user: User?,
    goToUser: (() -> Unit) ?= null,
    goToAllLots: () -> Unit,
    goToAboutMe: () -> Unit,
    addToSubscriptions: () -> Unit,
    goToSubscriptions: () -> Unit,
    goToSettings: (() -> Unit)? = null,
    isBlackList: ArrayList<String> // Suspend function to check black/white lists
) {
    if (user != null) {
        val userMod = if (goToUser != null){
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(dimens.mediumPadding).clickable { goToUser() }
        }else{
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(dimens.mediumPadding)
        }

        Column(
            modifier = modifier
        ) {
            // Header row with user details
            Row(
                modifier = userMod,
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    modifier = Modifier.wrapContentSize().weight(2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val image = user.avatar?.thumb?.content
                    if (image != null && image != "${SAPI.SERVER_BASE}images/no_avatar.svg") {
                        Card(
                            modifier = Modifier.padding(dimens.smallPadding),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            LoadImage(
                                url = image,
                                isShowLoading = false,
                                isShowEmpty = false,
                                size = 60.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(dimens.smallSpacer))
                    }

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = user.login ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.brightBlue,
                            modifier = Modifier.padding(dimens.smallPadding)
                        )

                        Spacer(modifier = Modifier.height(dimens.smallSpacer))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (user.rating > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            colors.ratingBlue,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .padding(dimens.smallPadding)
                                ) {
                                    Text(
                                        text = user.rating.toString(),
                                        color = colors.alwaysWhite,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(dimens.smallSpacer))
                            // user rating badge
                            if (user.ratingBadge?.imageUrl != null) {
                                LoadImage(
                                    user.ratingBadge.imageUrl,
                                    isShowLoading = false,
                                    isShowEmpty = false,
                                    size = dimens.mediumIconSize
                                )
                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                            }
                            Spacer(modifier = Modifier.width(dimens.smallSpacer))
                            // Verified user icon
                            if (user.isVerified) {
                                Image(
                                    painter = painterResource(drawables.verifySellersIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.mediumIconSize)
                                )
                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                            }
                        }
                    }
                }

                user.feedbackTotal?.let {
                    val s = if ((it.percentOfPositiveFeedbacks.mod(1.0)) > 0) {
                        it.percentOfPositiveFeedbacks.toString() + " %"
                    } else {
                        it.percentOfPositiveFeedbacks.roundToInt()
                            .toString() + " %"
                    }
                    Column(
                       modifier = Modifier.wrapContentSize().weight(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ){
                        DiscountBadge(s)

                        Text(
                            stringResource(strings.positiveFeedbackLabel),
                            color = colors.black,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                            textAlign = TextAlign.Center,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Image(
                                painterResource(drawables.miniLikeIcon),
                                contentDescription = "",
                            )
                            Spacer(modifier = Modifier.width(dimens.smallSpacer))
                            Text(
                                it.totalPercentage.roundToInt().toString(),
                                color = colors.black,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(dimens.smallSpacer))
                            Image(
                                painterResource(drawables.miniDislikeIcon),
                                contentDescription = "",
                            )

                            Spacer(modifier = Modifier.width(dimens.smallSpacer))

                            Text(
                                (it.totalPercentage - it.positiveFeedbacksCount).roundToInt()
                                    .toString(),
                                color = colors.black,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Button: "All Offers"
                SimpleTextButton(
                    text = stringResource(strings.allOffers),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    shape = MaterialTheme.shapes.medium,
                    onClick = goToAllLots
                )

                Spacer(modifier = Modifier.width(dimens.smallPadding))

                // Button: "About Me"
                SimpleTextButton(
                    text = stringResource(strings.aboutMeLabel),
                    backgroundColor = colors.textA0AE,
                    shape = MaterialTheme.shapes.medium,
                    onClick = goToAboutMe
                )

                Spacer(modifier = Modifier.width(dimens.smallPadding))
                // Button: "Subscriptions"

                if (user.followersCount != null) {
                    val subLabel = if (UserData.login == user.id){
                        stringResource(strings.subscribersLabel)
                    }else{
                        stringResource(strings.subscriptionsLabel)
                    }

                    FlowRow(
                        modifier = Modifier.wrapContentSize()
                            .shadow(dimens.smallElevation, shape = MaterialTheme.shapes.small)
                            .background(colors.grayLayout, shape = MaterialTheme.shapes.small)
                            .clickable {
                                if (UserData.login == user.id){
                                    goToSubscriptions()
                                }else {
                                    addToSubscriptions()
                                }
                            }
                            .padding(dimens.extraSmallPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = subLabel,
                            color = colors.black,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(dimens.smallPadding),
                        )
                        Row(
                            modifier = Modifier.wrapContentSize()
                                .background(colors.brightGreen, shape = MaterialTheme.shapes.medium)
                                .padding(dimens.extraSmallPadding)
                                .align(Alignment.CenterVertically),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                painterResource(drawables.vectorManSubscriptionIcon),
                                contentDescription = null,
                                tint = colors.alwaysWhite,
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )
                            Spacer(modifier = Modifier.width(dimens.smallPadding))
                            Text(
                                text = user.followersCount.toString(),
                                color = colors.alwaysWhite,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }else{
                    SmallIconButton(
                        drawables.subscriptionIcon,
                        color = colors.brightGreen
                    ){
                        addToSubscriptions()
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.smallPadding))

            // Check if the user is in the black list
            // Status annotation display based on the list type
            isBlackList.forEach { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (status) {
                                "blacklist_users" -> goToSettings?.invoke() // Blacklist users action
                                "blacklist_buyers" -> goToSettings?.invoke() // Blacklist buyers action
                                "whitelist_buyers" -> goToSettings?.invoke() // Whitelist buyers action
                            }
                        }
                        .padding(dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(drawables.infoIcon),
                        contentDescription = null,
                        tint = when (status) {
                            "blacklist_sellers", "blacklist_buyers" -> colors.notifyTextColor
                            "whitelist_buyers" -> colors.actionTextColor
                            else -> colors.notifyTextColor
                        },
                        modifier = Modifier.size(dimens.smallIconSize)
                    )

                    Spacer(modifier = Modifier.width(dimens.smallPadding))

                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(strings.publicBlockUserLabel))
                            append(" ")
                            withStyle(
                                style = SpanStyle(
                                    color = colors.black,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append(
                                    when (status) {
                                        "blacklist_sellers" -> stringResource(strings.blackListUserLabel)
                                        "blacklist_buyers" -> stringResource(strings.blackListUserLabel)
                                        "whitelist_buyers" -> stringResource(strings.whiteListUserLabel)
                                        else -> ""
                                    }
                                )
                            }
                        },
                        color = when (status) {
                            "blacklist_sellers", "blacklist_buyers" -> colors.notifyTextColor
                            "whitelist_buyers" -> colors.actionTextColor
                            else -> colors.notifyTextColor
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Display vacation or user status
            if (user.vacationEnabled) {
                Box(
                    modifier = Modifier
                        .padding(dimens.smallPadding)
                        .background(colors.outgoingBubble, shape = MaterialTheme.shapes.medium)
                        .clickable { goToSettings?.invoke() }
                ) {
                    val vacationMessage = buildAnnotatedString {
                        // Header
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.notifyTextColor, fontSize = dimens.largeText)) {
                            append(if (UserData.login == user.id) {
                                stringResource(strings.publicVacationMyLabel)
                            } else {
                                stringResource(strings.publicVacationHeaderLabel)
                            })
                        }
                        append("\n")

                        // From Date
                        withStyle(style = SpanStyle(color = colors.brightBlue, fontSize = dimens.largeText)) {
                            append("${stringResource(strings.fromAboutParameterName)} ")
                            append(user.vacationStart.toString().convertDateWithMinutes())
                        }
                        append(" ")

                        // To Date
                        withStyle(style = SpanStyle(color = colors.brightBlue, fontSize = dimens.largeText)) {
                            append("${stringResource(strings.toAboutParameterName)} ")
                            append(user.vacationEnd.toString().convertDateWithMinutes())
                        }
                        append("\n")

                        // Vacation Comment
                        if (user.vacationMessage?.isNotEmpty() == true) {
                            withStyle(style = SpanStyle(
                                fontStyle = FontStyle.Italic,
                                color = colors.grayText,
                                fontSize = dimens.largeText
                            )) {
                                append("${stringResource(strings.commentLabel)} ")
                                append(user.vacationMessage)
                            }
                        }
                    }

                    Text(
                        text = vacationMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.notifyTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            //registration date
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.smallPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(strings.createdUserLabel) + " " +
                            user.createdTs.toString().convertDateWithMinutes(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.black
                )

                Spacer(modifier = Modifier.height(dimens.smallPadding))

                Text(
                    stringResource(strings.lastActiveUserLabel) + " " +
                            user.lastActiveTs.toString().convertDateWithMinutes(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.black
                )
            }
        }
    }
}
