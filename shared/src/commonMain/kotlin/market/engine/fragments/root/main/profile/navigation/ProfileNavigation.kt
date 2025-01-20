package market.engine.fragments.root.main.profile.navigation

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
import market.engine.core.data.items.SelectedBasketItem
import market.engine.fragments.createOffer.createOfferFactory
import market.engine.fragments.listing.listingFactory
import market.engine.fragments.offer.offerFactory
import market.engine.fragments.user.userFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.createOrder.CreateOrderComponent
import market.engine.fragments.createOrder.CreateOrderContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.root.main.profile.DefaultProfileComponent
import market.engine.fragments.root.main.profile.ProfileComponent
import market.engine.fragments.root.main.profile.ProfileContent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent
import market.engine.fragments.createOrder.createOrderFactory
import market.engine.fragments.root.main.profile.conversations.ConversationsComponent
import market.engine.fragments.root.main.profile.conversations.ConversationsContent
import market.engine.fragments.root.main.profile.conversations.conversationsFactory

@Serializable
sealed class ProfileConfig {
    @Serializable
    data class ProfileScreen(val openPage : String? = null) : ProfileConfig()
    @Serializable
    data object MyOffersScreen : ProfileConfig()

    @Serializable
    data object ConversationsScreen : ProfileConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : ProfileConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : ProfileConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe: Boolean) : ProfileConfig()

    @Serializable
    data class MyOrdersScreen(
        val type : DealTypeGroup
    ) : ProfileConfig()

    @Serializable
    data class CreateOfferScreen(
        val catPath: List<Long>?,
        val offerId: Long? = null,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>? = null
    ) : ProfileConfig()

    @Serializable
    data class CreateOrderScreen(
        val basketItem : Pair<Long, List<SelectedBasketItem>>,
    ) : ProfileConfig()
}

sealed class ChildProfile {
    class ProfileChild(val component: ProfileComponent) : ChildProfile()
    class MyOffersChild(val component: ProfileComponent) : ChildProfile()
    class OfferChild(val component: OfferComponent) : ChildProfile()
    class ListingChild(val component: ListingComponent) : ChildProfile()
    class UserChild(val component: UserComponent) : ChildProfile()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildProfile()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildProfile()
    class MyOrdersChild(val type : DealTypeGroup, val component: ProfileComponent) : ChildProfile()
    class ConversationsChild(val component: ConversationsComponent) : ChildProfile()
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
            is ChildProfile.CreateOrderChild -> CreateOrderContent(screen.component)
            is ChildProfile.MyOrdersChild -> ProfileMyOrdersNavigation(screen.type, screen.component, modifier)
            is ChildProfile.ConversationsChild -> ConversationsContent(screen.component, modifier)
        }
    }
}

fun createProfileChild(
    config: ProfileConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    navigateToMyOrders: () -> Unit
): ChildProfile {

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
                    ProfileConfig.CreateOfferScreen(null, null, CreateOfferType.CREATE, null)
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
            isVisible = false,
            badgeCount = if((userInfo?.countUnreadPriceProposals ?:0) > 0)
                userInfo?.countUnreadPriceProposals else null
        ),
        NavigationItem(
            title = strings.myPurchasesTitle,
            subtitle = strings.myPurchasesSubTitle,
            icon = drawables.purchasesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                try {
                    profileNavigation.replaceCurrent(
                        ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY)
                    )
                } catch (_: Exception) {
                }
            }
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
            badgeCount = null,
            onClick = {
                try {
                    profileNavigation.replaceCurrent(
                        ProfileConfig.MyOrdersScreen(DealTypeGroup.SELL)
                    )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = strings.messageTitle,
            subtitle = strings.messageSubTitle,
            icon = drawables.dialogIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = userInfo?.countUnreadMessages,
            onClick = {
                try {
                    profileNavigation.replaceCurrent(
                        ProfileConfig.ConversationsScreen
                    )
                } catch ( _ : Exception){
                }
            }
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

    return when (config) {
        is ProfileConfig.ProfileScreen -> ChildProfile.ProfileChild(
            itemProfile(
                componentContext,
                profileNavigation,
                profilePublicNavigationList.value,
                selectedPage = config.openPage
            )
        )

        ProfileConfig.MyOffersScreen -> ChildProfile.MyOffersChild(
            component = itemProfile(componentContext, profileNavigation, profilePublicNavigationList.value)
        )

        is ProfileConfig.OfferScreen -> ChildProfile.OfferChild(
            component = offerFactory(
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
                navigateToCreateOffer = { type, catPath, offerId, externalImages ->
                    profileNavigation.pushNew(
                        ProfileConfig.CreateOfferScreen(
                            catPath = catPath,
                            createOfferType = type,
                            externalImages = externalImages,
                            offerId = offerId
                        )
                    )
                },
                navigateToCreateOrder = {
                    profileNavigation.pushNew(
                        ProfileConfig.CreateOrderScreen(it)
                    )
                }
            )
        )

        is ProfileConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = MutableValue(config.searchData),
                data = MutableValue(config.listingData)
            )
            ChildProfile.ListingChild(
                component = listingFactory(
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
                component = userFactory(
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
            component = createOfferFactory(
                componentContext = componentContext,
                catPath = config.catPath,
                offerId = config.offerId,
                type = config.createOfferType,
                externalImages = config.externalImages,
                navigateOffer = { id ->
                    profileNavigation.pushNew(
                        ProfileConfig.OfferScreen(id, getCurrentDate())
                    )
                },
                navigateCreateOffer = { id, path, t ->
                    profileNavigation.replaceCurrent(
                        ProfileConfig.CreateOfferScreen(
                            catPath = path,
                            offerId = id,
                            createOfferType = t,
                        )
                    )
                },
            ) {
                profileNavigation.pop()
            }
        )

        is ProfileConfig.CreateOrderScreen -> ChildProfile.CreateOrderChild(
            component = createOrderFactory(
                componentContext = componentContext,
                selectedItems = config.basketItem,
                navigateUser = {
                    profileNavigation.pushNew(
                        ProfileConfig.UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateOffer = {
                    profileNavigation.pushNew(
                        ProfileConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateBack = {
                    profileNavigation.pop()
                },
                navigateToMyOrders = {
                    navigateToMyOrders()
                }
            )
        )

        is ProfileConfig.MyOrdersScreen -> ChildProfile.MyOrdersChild(
            config.type,
            component = itemProfile(componentContext, profileNavigation,profilePublicNavigationList.value, config.type)
        )

        ProfileConfig.ConversationsScreen -> ChildProfile.ConversationsChild(
            component = conversationsFactory(
                componentContext,
                profilePublicNavigationList.value
            )
        )
    }
}

fun itemProfile(
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    navigationItems : List<NavigationItem>,
    selectedOrderPage : DealTypeGroup = DealTypeGroup.SELL,
    selectedPage : String? = null
): ProfileComponent {
    return DefaultProfileComponent(
        componentContext = componentContext,
        navigationItems = navigationItems,
        profileNavigation,
        selectedOrderPage,
        selectedPage = selectedPage
    )
}
