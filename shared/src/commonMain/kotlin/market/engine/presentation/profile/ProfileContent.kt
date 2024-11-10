package market.engine.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.core.repositories.UserRepository
import market.engine.presentation.base.BaseContent
import market.engine.presentation.main.bottomBar
import market.engine.presentation.profileMyOffers.MyOffersContent
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.exceptions.onError
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ProfileContent(
    component: ProfileComponent,
    modifier: Modifier
) {
//    val modelState = component.model.subscribeAsState()
//    val model = modelState.value
//    val profileViewModel = model.profileViewModel
//    val isLoading = profileViewModel.isShowProgress.collectAsState()
//    val err = profileViewModel.errorMessage.collectAsState()
    val scrollState = rememberScrollState()
    val userInfo = UserData.userInfo
    val userRepository : UserRepository = koinInject()
    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }
//
//    val error: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
//        { onError(err.value) {  } }
//    } else {
//        null
//    }

    val list = listOf(
        NavigationItem(
            title = stringResource(strings.createNewOfferTitle),
            icon = drawables.newLotIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = null,
            onClick = {

            }
        ),
        NavigationItem(
            title = stringResource(strings.myBidsTitle),
            subtitle = stringResource(strings.myBidsSubTitle),
            icon = drawables.bidsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.proposalTitle),
            subtitle = stringResource(strings.proposalPriceSubTitle),
            icon = drawables.proposalIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countUnreadPriceProposals ?:0) > 0)
                userInfo?.countUnreadPriceProposals else null
        ),
        NavigationItem(
            title = stringResource(strings.myPurchasesTitle),
            subtitle = stringResource(strings.myPurchasesSubTitle),
            icon = drawables.purchasesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.myOffersTitle),
            subtitle = stringResource(strings.myOffersSubTitle),
            icon = drawables.tagIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                component.navigateToMyOffers()
            }
        ),
        NavigationItem(
            title = stringResource(strings.mySalesTitle),
            subtitle = stringResource(strings.mySalesSubTitle),
            icon = drawables.salesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.messageTitle),
            subtitle = stringResource(strings.messageSubTitle),
            icon = drawables.dialogIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = userInfo?.countUnreadMessages
        ),
        NavigationItem(
            title = stringResource(strings.myProfileTitle),
            subtitle = stringResource(strings.myProfileSubTitle),
            icon = drawables.profileIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.settingsProfileTitle),
            subtitle = stringResource(strings.profileSettingsSubTitle),
            icon = drawables.settingsIcon,
            tint = colors.black,
            hasNews = true,
            badgeCount = null
        ),
        NavigationItem(
            title = "${stringResource(strings.myBalanceTitle)} ${userInfo?.balance} ${
                stringResource(
                    strings.currencyCode
                )
            }",
            subtitle = stringResource(strings.myBalanceSubTitle),
            icon = drawables.balanceIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.logoutTitle),
            icon = drawables.logoutIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
    )

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        showVerticalScrollbarState = scrollState,
        bottomBar = bottomBar,
        onRefresh = {
            userRepository.updateToken()
            userRepository.updateUserInfo(scope)
        },
        error = null
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = true,
            enter = expandIn(),
            exit = fadeOut()
        ) {
            Column {
                Row(
                    modifier = modifier.fillMaxWidth().padding(dimens.smallPadding),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding, Alignment.CenterHorizontally),
                ) {
                    val image = userInfo?.avatar?.thumb?.content

                    Card(
                        modifier = Modifier.padding(dimens.smallPadding),
                        shape = MaterialTheme.shapes.extraLarge
                    ){
                        if (image != null) {
                            LoadImage(
                                url = image,
                                size = 60.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(drawables.profileIcon),
                                contentDescription = "",
                                tint = colors.black,
                                modifier = Modifier.size(dimens.mediumIconSize)
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row {
                            TitleText(
                                text = userInfo?.login.toString()
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.padding(dimens.smallPadding)
                        ) {
                            Text(
                                stringResource(strings.ratingParameterName)
                            )

                            TitleText(
                                text = userInfo?.rating.toString(),
                                color = colors.ratingBlue
                            )

                            if (userInfo?.isVerified == true) {
                                Image(
                                    painterResource(drawables.verifySellersIcon),
                                    contentDescription = "",
                                    modifier = Modifier.size(dimens.smallIconSize)
                                )
                            }

                            Spacer(modifier = Modifier.width(dimens.smallPadding))

                            val imageRating = UserData.userInfo?.ratingBadge?.imageUrl

                            if (imageRating != null) {
                                LoadImage(
                                    url = imageRating,
                                    size = 30.dp
                                )
                            }

                            Spacer(modifier = Modifier.width(dimens.smallPadding))

                            SimpleTextButton(
                                text = "id",
                                shape = MaterialTheme.shapes.large
                            ){

                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    list.forEachIndexed { _, item ->
                        Spacer(modifier = Modifier.height(dimens.smallSpacer))

                        NavigationDrawerItem(
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.Start
                                    ){
                                        Text(
                                            item.title,
                                            color = colors.black,
                                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                            lineHeight = dimens.largeText,
                                        )
                                        if (item.subtitle != null) {
                                            Text(
                                                item.subtitle,
                                                color = colors.steelBlue,
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                                lineHeight = dimens.largeText
                                            )
                                        }
                                    }

                                }
                            },
                            onClick = item.onClick,
                            icon = {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = item.title,
                                    modifier = Modifier.size(dimens.smallIconSize),
                                    tint = item.tint
                                )
                            },
                            badge = {
                                if (item.badgeCount != null) {
                                    Badge {
                                        Text(text = item.badgeCount.toString())
                                    }
                                }

                                if (item.hasNews) {
                                    Badge {  }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = colors.white,
                                unselectedContainerColor = colors.white,
                                selectedIconColor = colors.textA0AE,
                                unselectedIconColor = colors.textA0AE,
                                selectedTextColor = colors.black,
                                selectedBadgeColor = colors.black,
                                unselectedTextColor = colors.black,
                                unselectedBadgeColor = colors.black
                            ),
                            shape = MaterialTheme.shapes.small,
                            selected = true
                        )
                    }
                    Spacer(modifier = Modifier.height(dimens.mediumSpacer))
                }
            }
        }
    }


}
