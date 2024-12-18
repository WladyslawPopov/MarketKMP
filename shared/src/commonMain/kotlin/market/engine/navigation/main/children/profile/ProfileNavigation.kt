package market.engine.navigation.main.children.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.NavigationItem
import market.engine.navigation.main.publicItems.itemCreateOffer
import market.engine.navigation.main.publicItems.itemListing
import market.engine.navigation.main.publicItems.itemOffer
import market.engine.navigation.main.publicItems.itemUser
import market.engine.core.data.types.CreateOfferType
import market.engine.core.util.getCurrentDate
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.profile.DefaultProfileComponent
import market.engine.fragments.profile.ProfileComponent
import market.engine.fragments.profile.ProfileContent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent

@Serializable
sealed class ProfileConfig {
    @Serializable
    data object ProfileScreen : ProfileConfig()
    @Serializable
    data object MyOffersScreen : ProfileConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : ProfileConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : ProfileConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe: Boolean) : ProfileConfig()

    @Serializable
    data class CreateOfferScreen(
        val categoryId: Long,
        val offerId: Long? = null,
        val type : CreateOfferType,
        val externalImages : List<String>? = null
    ) : ProfileConfig()
}

sealed class ChildProfile {
    class ProfileChild(val component: ProfileComponent) : ChildProfile()
    class MyOffersChild(val component: ProfileComponent) : ChildProfile()
    class OfferChild(val component: OfferComponent) : ChildProfile()
    class ListingChild(val component: ListingComponent) : ChildProfile()
    class UserChild(val component: UserComponent) : ChildProfile()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildProfile()
}

@Composable
fun ProfileNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildProfile>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildProfile.ProfileChild -> ProfileContent(screen.component, modifier)
            is ChildProfile.MyOffersChild -> ProfileMyOffersNavigation(screen.component, modifier)
            is ChildProfile.OfferChild -> OfferContent(screen.component, modifier)
            is ChildProfile.ListingChild -> ListingContent(screen.component, modifier)
            is ChildProfile.UserChild -> UserContent(screen.component, modifier)
            is ChildProfile.CreateOfferChild -> CreateOfferContent(screen.component)
        }
    }
}

fun createProfileChild(
    config: ProfileConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>
): ChildProfile =
    when (config) {
        ProfileConfig.ProfileScreen -> ChildProfile.ProfileChild(
            itemProfile(componentContext, profileNavigation)
        )

        ProfileConfig.MyOffersScreen -> ChildProfile.MyOffersChild(
            component = itemProfile(componentContext, profileNavigation)
        )

        is ProfileConfig.OfferScreen -> ChildProfile.OfferChild(
            component = itemOffer(
                componentContext,
                config.id,
                selectOffer = {
                    profileNavigation.pushNew(ProfileConfig.OfferScreen(it, getCurrentDate()))
                },
                onBack = {
                    profileNavigation.pop()
                },
                onListingSelected = { ld ->
                    profileNavigation.pushNew(
                        ProfileConfig.ListingScreen(
                            ld.data.value,
                            ld.searchData.value
                        )
                    )
                },
                onUserSelected = { ui, about ->
                    profileNavigation.pushNew(ProfileConfig.UserScreen(ui, getCurrentDate(), about))
                },
                isSnapshot = config.isSnapshot,
                navigateToCreateOffer = { type, offerId, externalImages ->
                    profileNavigation.pushNew(
                        ProfileConfig.CreateOfferScreen(
                            categoryId = 1L,
                            type = type,
                            externalImages = externalImages,
                            offerId = offerId
                        )
                    )
                }
            )
        )

        is ProfileConfig.ListingScreen -> {
            val ld = ListingData(
                _searchData = config.searchData,
                _data = config.listingData
            )
            ChildProfile.ListingChild(
                component = itemListing(
                    componentContext,
                    ld,
                    selectOffer = {
                        profileNavigation.pushNew(
                            ProfileConfig.OfferScreen(it, getCurrentDate())
                        )
                    },
                    onBack = {
                        profileNavigation.pop()
                    },
                    isOpenCategory = false,
                )
            )
        }

        is ProfileConfig.UserScreen -> {
            ChildProfile.UserChild(
                component = itemUser(
                    componentContext,
                    config.userId,
                    config.aboutMe,
                    goToLogin = {
                        profileNavigation.pushNew(
                            ProfileConfig.ListingScreen(it.data.value, it.searchData.value)
                        )
                    },
                    goBack = {
                        profileNavigation.pop()
                    },
                    goToSnapshot = { id ->
                        profileNavigation.pushNew(
                            ProfileConfig.OfferScreen(id, getCurrentDate(), true)
                        )
                    },
                    goToUser = {
                        profileNavigation.pushNew(
                            ProfileConfig.UserScreen(it, getCurrentDate(), false)
                        )
                    }
                )
            )
        }

        is ProfileConfig.CreateOfferScreen -> ChildProfile.CreateOfferChild(
            component = itemCreateOffer(
                componentContext = componentContext,
                categoryId = config.categoryId,
                offerId = config.offerId,
                type = config.type,
                externalImages = config.externalImages,
            ) {
                profileNavigation.pop()
            }
        )
    }

fun itemProfile(
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>
): ProfileComponent {

    val userInfo = UserData.userInfo

    val profilePublicNavigationList = MutableValue(listOf(
        NavigationItem(
            title = strings.createNewOfferTitle,
            icon = drawables.newLotIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = null,
            onClick = {
                profileNavigation.pushNew(
                    ProfileConfig.CreateOfferScreen(1L, null, CreateOfferType.CREATE, null)
                )
            }
        ),
        NavigationItem(
            title = strings.myBidsTitle,
            subtitle = strings.myBidsSubTitle,
            icon = drawables.bidsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.proposalTitle,
            subtitle = strings.proposalPriceSubTitle,
            icon = drawables.proposalIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countUnreadPriceProposals ?:0) > 0)
                userInfo?.countUnreadPriceProposals else null
        ),
        NavigationItem(
            title = strings.myPurchasesTitle,
            subtitle = strings.myPurchasesSubTitle,
            icon = drawables.purchasesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.myOffersTitle,
            subtitle = strings.myOffersSubTitle,
            icon = drawables.tagIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                try {
                    profileNavigation.replaceCurrent(
                        ProfileConfig.MyOffersScreen
                    )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = strings.mySalesTitle,
            subtitle = strings.mySalesSubTitle,
            icon = drawables.salesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.messageTitle,
            subtitle = strings.messageSubTitle,
            icon = drawables.dialogIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = userInfo?.countUnreadMessages
        ),
        NavigationItem(
            title = strings.myProfileTitle,
            subtitle = strings.myProfileSubTitle,
            icon = drawables.profileIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                try {
                   profileNavigation.pushNew(
                       ProfileConfig.UserScreen(
                           UserData.login,
                           getCurrentDate(),
                           false
                       )
                   )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = strings.settingsProfileTitle,
            subtitle = strings.profileSettingsSubTitle,
            icon = drawables.settingsIcon,
            tint = colors.black,
            hasNews = true,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.myBalanceTitle,
            subtitle = strings.myBalanceSubTitle,
            icon = drawables.balanceIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.logoutTitle,
            icon = drawables.logoutIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {

            }
        ),
    ))

    return DefaultProfileComponent(
        componentContext = componentContext,
        navigationItems = profilePublicNavigationList.value,
        profileNavigation
    )
}
