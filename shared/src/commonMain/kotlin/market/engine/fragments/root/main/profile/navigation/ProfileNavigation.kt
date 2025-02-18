package market.engine.fragments.root.main.profile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.fragments.root.main.user.userFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.createOffer.CreateOfferComponent
import market.engine.fragments.root.main.createOffer.CreateOfferContent
import market.engine.fragments.root.main.createOffer.createOfferFactory
import market.engine.fragments.root.main.createOrder.CreateOrderComponent
import market.engine.fragments.root.main.createOrder.CreateOrderContent
import market.engine.fragments.root.main.createOrder.createOrderFactory
import market.engine.fragments.root.main.listing.ListingComponent
import market.engine.fragments.root.main.listing.ListingContent
import market.engine.fragments.root.main.listing.listingFactory
import market.engine.fragments.root.main.messenger.DialogsComponent
import market.engine.fragments.root.main.messenger.DialogsContent
import market.engine.fragments.root.main.messenger.messengerFactory
import market.engine.fragments.root.main.offer.OfferComponent
import market.engine.fragments.root.main.offer.OfferContent
import market.engine.fragments.root.main.offer.offerFactory
import market.engine.fragments.root.main.profile.DefaultProfileComponent
import market.engine.fragments.root.main.profile.ProfileComponent
import market.engine.fragments.root.main.profile.ProfileContent
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.profile.conversations.ConversationsComponent
import market.engine.fragments.root.main.profile.conversations.ConversationsContent
import market.engine.fragments.root.main.profile.conversations.conversationsFactory
import market.engine.fragments.root.main.profile.DefaultProfileChildrenComponent
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.proposalPage.ProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalContent
import market.engine.fragments.root.main.proposalPage.proposalFactory
import market.engine.fragments.root.main.user.UserContent
import org.jetbrains.compose.resources.stringResource

@Serializable
sealed class ProfileConfig {
    @Serializable
    data class ProfileScreen(val openPage : String? = null) : ProfileConfig()
    @Serializable
    data object MyOffersScreen : ProfileConfig()
    @Serializable
    data object MyBidsScreen : ProfileConfig()
    @Serializable
    data object MyProposalsScreen : ProfileConfig()

    @Serializable
    data object ProfileSettingsScreen : ProfileConfig()

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
        val typeGroup : DealTypeGroup,
        val id: Long? = null
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

    @Serializable
    data class DialogsScreen(val dialogId : Long) : ProfileConfig()
    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType, val ts: String) : ProfileConfig()
}

sealed class ChildProfile {
    class ProfileChild(val component: ProfileComponent) : ChildProfile()
    class MyOffersChild(val component: ProfileChildrenComponent) : ChildProfile()
    class ProfileSettingsChild(val component: ProfileChildrenComponent) : ChildProfile()
    class OfferChild(val component: OfferComponent) : ChildProfile()
    class ListingChild(val component: ListingComponent) : ChildProfile()
    class UserChild(val component: UserComponent) : ChildProfile()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildProfile()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildProfile()
    class MyOrdersChild(val type : DealTypeGroup, val component: ProfileChildrenComponent) : ChildProfile()
    class ConversationsChild(val component: ConversationsComponent) : ChildProfile()
    class MyBidsChild(val component: ProfileChildrenComponent) : ChildProfile()
    class DialogsChild(val component: DialogsComponent) : ChildProfile()
    class MyProposalsChild(val component: ProfileChildrenComponent) : ChildProfile()
    class ProposalChild(val component: ProposalComponent) : ChildProfile()
}

@Composable
fun ProfileNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildProfile>>,
    profileNavigation: StackNavigation<ProfileConfig>
) {
    val stack by childStack.subscribeAsState()

    val userInfo = UserData.userInfo
    val publicProfileNavigationItems = MutableValue(listOf(
        NavigationItem(
            title = stringResource(strings.createNewOfferTitle),
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
            title = stringResource(strings.myBidsTitle),
            subtitle = stringResource(strings.myBidsSubTitle),
            icon = drawables.bidsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                try {
                    profileNavigation.replaceCurrent(
                        ProfileConfig.MyBidsScreen
                    )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = stringResource(strings.proposalTitle),
            subtitle = stringResource(strings.proposalPriceSubTitle),
            icon = drawables.proposalIcon,
            tint = colors.black,
            hasNews = false,
            isVisible = true,
            badgeCount = if((userInfo?.countUnreadPriceProposals ?:0) > 0)
                userInfo?.countUnreadPriceProposals else null,
            onClick = {
                try {
                    profileNavigation.replaceCurrent(
                        ProfileConfig.MyProposalsScreen
                    )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = stringResource(strings.myPurchasesTitle),
            subtitle = stringResource(strings.myPurchasesSubTitle),
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
            title = stringResource(strings.myOffersTitle),
            subtitle = stringResource(strings.myOffersSubTitle),
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
            title = stringResource(strings.mySalesTitle),
            subtitle = stringResource(strings.mySalesSubTitle),
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
            title = stringResource(strings.messageTitle),
            subtitle = stringResource(strings.messageSubTitle),
            icon = drawables.dialogIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countUnreadMessages ?:0) > 0) userInfo?.countUnreadMessages else null,
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
            title = stringResource(strings.myProfileTitle),
            subtitle = stringResource(strings.myProfileSubTitle),
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
            title = stringResource(strings.settingsProfileTitle),
            subtitle = stringResource(strings.profileSettingsSubTitle),
            icon = drawables.settingsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                try {
                    profileNavigation.replaceCurrent(
                        ProfileConfig.ProfileSettingsScreen
                    )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = stringResource(strings.myBalanceTitle),
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
            badgeCount = null,
            onClick = {
                goToLogin(true)
            }
        ),
    ))

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildProfile.ProfileChild -> ProfileContent(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.MyOffersChild -> ProfileMyOffersNavigation(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.OfferChild -> OfferContent(screen.component, modifier)
            is ChildProfile.ListingChild -> ListingContent(screen.component, modifier)
            is ChildProfile.UserChild -> UserContent(screen.component, modifier)
            is ChildProfile.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildProfile.CreateOrderChild -> CreateOrderContent(screen.component)
            is ChildProfile.MyOrdersChild -> ProfileMyOrdersNavigation(screen.type, screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.ConversationsChild -> ConversationsContent(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.MyBidsChild -> ProfileMyBidsNavigation(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.DialogsChild -> DialogsContent(screen.component, modifier)
            is ChildProfile.ProfileSettingsChild -> ProfileSettingsNavigation(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.MyProposalsChild -> ProfileMyProposalsNavigation(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.ProposalChild -> ProposalContent(screen.component)
        }
    }
}

fun createProfileChild(
    config: ProfileConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToSubscribe: () -> Unit
): ChildProfile {

    return when (config) {
        is ProfileConfig.ProfileScreen -> ChildProfile.ProfileChild(
            itemProfile(
                componentContext,
                config.openPage,
                profileNavigation,
                navigateToSubscribe
            )
        )

        ProfileConfig.MyOffersScreen -> ChildProfile.MyOffersChild(
            component = DefaultProfileChildrenComponent(
                null,
                componentContext,
                profileNavigation,
            )
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
                },
                navigateToLogin = {
                    goToLogin(false)
                },
                navigateToDialog = { dialogId ->
                    if(dialogId != null)
                        profileNavigation.pushNew(ProfileConfig.DialogsScreen(dialogId))
                    else
                        profileNavigation.replaceCurrent(ProfileConfig.ConversationsScreen)
                },
                navigationSubscribes = {
                    navigateToSubscribe()
                },
                navigateToProposalPage = { offerId, type ->
                    profileNavigation.pushNew(
                        ProfileConfig.ProposalScreen(offerId, type, getCurrentDate())
                    )
                }
            )
        )

        is ProfileConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = mutableStateOf(config.searchData),
                data = mutableStateOf(config.listingData)
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
                    navigateToSubscribe = {
                        navigateToSubscribe()
                    }
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
                    },
                    goToSubscriptions = {
                        navigateToSubscribe()
                    },
                    goToOrder = { id, type ->
                        navigateToMyOrders(id, type)
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
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

        is ProfileConfig.MyOrdersScreen -> ChildProfile.MyOrdersChild(
            config.typeGroup,
            component = DefaultProfileChildrenComponent(
                if(config.typeGroup == DealTypeGroup.BUY) "purchases/${config.id}" else "sales/${config.id}",
                componentContext,
                profileNavigation,
            )
        )

        is ProfileConfig.ConversationsScreen -> ChildProfile.ConversationsChild(
            component = conversationsFactory(
                componentContext,
                navigateBack = {
                    profileNavigation.replaceAll(ProfileConfig.ProfileScreen())
                },
                navigateToMessenger = {
                    profileNavigation.pushNew(
                        ProfileConfig.DialogsScreen(it)
                    )
                },
            )
        )

        ProfileConfig.MyBidsScreen -> ChildProfile.MyBidsChild(
            DefaultProfileChildrenComponent(
                null,
                componentContext,
                profileNavigation,
            )
        )

        is ProfileConfig.DialogsScreen -> ChildProfile.DialogsChild(
            messengerFactory(
                componentContext,
                config.dialogId,
                navigateBack = {
                    profileNavigation.pop()
                },
                navigateToOrder = { id, type ->
                    profileNavigation.replaceAll(
                        ProfileConfig.MyOrdersScreen(
                            type, id
                        )
                    )
                },
                navigateToOffer = {
                    profileNavigation.pushNew(
                        ProfileConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToUser = {
                    profileNavigation.pushNew(
                        ProfileConfig.UserScreen(it, getCurrentDate(), false)
                    )
                }
            )
        )

        ProfileConfig.ProfileSettingsScreen -> ChildProfile.ProfileSettingsChild(
            DefaultProfileChildrenComponent(
                null,
                componentContext,
                profileNavigation,
            )
        )

        ProfileConfig.MyProposalsScreen -> ChildProfile.MyProposalsChild(
            DefaultProfileChildrenComponent(
                null,
                componentContext,
                profileNavigation,
            )
        )

        is ProfileConfig.ProposalScreen -> ChildProfile.ProposalChild(
            component = proposalFactory(
                config.offerId,
                config.proposalType,
                componentContext,
                navigateBack = {
                    profileNavigation.pop()
                },
                navigateToOffer = {
                    profileNavigation.pushNew(
                        ProfileConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToUser = {
                    profileNavigation.pushNew(
                        ProfileConfig.UserScreen(it, getCurrentDate(), false)
                    )
                }
            )
        )
    }
}

fun itemProfile(
    componentContext: ComponentContext,
    selectedPage : String? = null,
    profileNavigation: StackNavigation<ProfileConfig>,
    navigateToSubscriptions: () -> Unit
): ProfileComponent {
    return DefaultProfileComponent(
        componentContext,
        selectedPage,
        profileNavigation,
        navigateToSubscriptions
    )
}
