package market.engine.widgets.bars

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.User
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.idButton
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserPanel(
    modifier: Modifier,
    user: User?,
    updateTrigger: Int,
    goToUser: () -> Unit,
    goToAllLots: (() -> Unit)? = null,
    goToAboutMe: (() -> Unit)? = null,
    addToSubscriptions: (() -> Unit)? = null,
    goToSubscriptions: (() -> Unit)? = null,
    goToSettings: ((String) -> Unit) = {},
    isBlackList: List<String> = emptyList()
) {
    if (user != null && updateTrigger >= 0) {
        FlowRow(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally)
        ) {
            // Header row with user details
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { goToUser() }
                    .padding(dimens.smallPadding),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.weight(1f, false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer)
                ) {
                    Card(
                        shape = CircleShape
                    ) {
                        LoadImage(
                            url = user.avatar?.thumb?.content ?: "",
                            isShowLoading = false,
                            isShowEmpty = true,
                            size = 60.dp
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = user.login ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.brightBlue
                        )

                        FlowRow(
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.spacedBy(
                                dimens.extraSmallPadding,
                                alignment = Alignment.CenterHorizontally
                            )
                        ) {
                            if ((user.rating ?: 0) > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            colors.ratingBlue,
                                            shape = MaterialTheme.shapes.small
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
                            // user rating badge
                            if (user.ratingBadge?.imageUrl != null) {
                                LoadImage(
                                    user.ratingBadge.imageUrl,
                                    isShowLoading = false,
                                    isShowEmpty = false,
                                    size = dimens.mediumIconSize
                                )
                            }
                            // Verified user icon
                            if (user.isVerified) {
                                Image(
                                    painter = painterResource(drawables.verifiedIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.mediumIconSize)
                                )
                            }

                            idButton(user.id.toString())
                        }
                    }
                }

                user.feedbackTotal?.let {
                    val s = remember {
                        if ((it.percentOfPositiveFeedbacks.mod(1.0)) > 0) {
                            it.percentOfPositiveFeedbacks.toString() + " %"
                        } else {
                            it.percentOfPositiveFeedbacks.roundToInt()
                                .toString() + " %"
                        }
                    }

                    Column(
                        modifier = Modifier.wrapContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
                    ){
                        DiscountBadge(s)

                        Text(
                            stringResource(strings.positiveFeedbackLabel),
                            color = colors.black,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = dimens.smallText),
                            textAlign = TextAlign.Center,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer)
                        ) {
                            Image(
                                painterResource(drawables.miniLikeImage),
                                contentDescription = "",
                            )
                            Text(
                                it.totalPercentage.roundToInt().toString(),
                                color = colors.black,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Image(
                                painterResource(drawables.miniDislikeImage),
                                contentDescription = "",
                            )
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

            if(goToSubscriptions != null || goToAboutMe != null || goToAllLots != null || addToSubscriptions != null) {
                Column(
                    modifier = Modifier.padding(dimens.smallPadding),
                    verticalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        if (goToAllLots != null) {
                            // Button: "All Offers"
                            SimpleTextButton(
                                text = stringResource(strings.allOffers),
                                backgroundColor = colors.inactiveBottomNavIconColor,
                                onClick = goToAllLots
                            )
                        }
                        if (goToAboutMe != null) {
                            // Button: "About Me"
                            SimpleTextButton(
                                text = stringResource(strings.aboutMeLabel),
                                backgroundColor = colors.steelBlue,
                                textColor = colors.alwaysWhite,
                                onClick = goToAboutMe
                            )
                        }

                        if (user.followersCount != null) {
                            val subLabel = if (UserData.login == user.id) {
                                stringResource(strings.subscribersLabel)
                            } else {
                                stringResource(strings.subscriptionsLabel)
                            }
                            if (goToSubscriptions != null || addToSubscriptions != null) {
                                FlowRow(
                                    modifier = Modifier
                                        .shadow(
                                            dimens.smallElevation,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .background(
                                            colors.grayLayout,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable {
                                            if (UserData.login == user.id) {
                                                goToSubscriptions?.invoke()
                                            } else {
                                                addToSubscriptions?.invoke()
                                            }
                                        }
                                        .padding(dimens.smallPadding),
                                    verticalArrangement = Arrangement.spacedBy(
                                        dimens.smallPadding,
                                        alignment = Alignment.CenterVertically
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        dimens.extraSmallPadding,
                                        alignment = Alignment.CenterHorizontally
                                    )
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f, false),
                                        text = subLabel,
                                        color = colors.black,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )

                                    Row(
                                        modifier = Modifier.background(
                                            colors.brightGreen,
                                            shape = MaterialTheme.shapes.medium
                                        ).padding(dimens.extraSmallPadding),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                    ) {
                                        Icon(
                                            painterResource(drawables.vectorManSubscriptionIcon),
                                            contentDescription = null,
                                            tint = colors.alwaysWhite,
                                        )
                                        Text(
                                            text = user.followersCount.toString(),
                                            color = colors.alwaysWhite,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        } else {
                            if (addToSubscriptions != null) {
                                SmallIconButton(
                                    drawables.subscriptionIcon,
                                    color = colors.brightGreen
                                ) {
                                    addToSubscriptions()
                                }
                            }
                        }
                    }

                    //registration date
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer)
                    ) {
                        Text(
                            stringResource(strings.createdUserLabel) + " " +
                                    user.createdTs.toString().convertDateWithMinutes(),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.black
                        )

                        Text(
                            stringResource(strings.lastActiveUserLabel) + " " +
                                    user.lastActiveTs.toString().convertDateWithMinutes(),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.black
                        )
                    }
                }
            }
            // Check if the user is in the black list
            // Status annotation display based on the list type
            isBlackList.forEach { status ->
                Row(
                    modifier = Modifier
                        .clickable {
                            when (status) {
                                "blacklist_sellers" -> goToSettings("add_to_seller_blacklist") // Blacklist sellers action
                                "blacklist_buyers" -> goToSettings("add_to_buyer_blacklist") // Blacklist buyers action
                                "whitelist_buyers" -> goToSettings("add_to_whitelist" ) // Whitelist buyers action
                            }
                        }.padding(dimens.mediumPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
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
                        .background(colors.transparentGrayColor, shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            if (UserData.login == user.id) {
                                goToSettings("set_vacation")
                            }
                        }
                        .padding(dimens.mediumPadding)
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
        }
    }
}
