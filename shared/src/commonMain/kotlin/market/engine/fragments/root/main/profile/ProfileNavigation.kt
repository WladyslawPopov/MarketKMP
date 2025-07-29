package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.common.backAnimation
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.createOffer.CreateOfferComponent
import market.engine.fragments.root.main.createOffer.CreateOfferContent
import market.engine.fragments.root.main.createOffer.DefaultCreateOfferComponent
import market.engine.fragments.root.main.createOrder.CreateOrderComponent
import market.engine.fragments.root.main.createOrder.CreateOrderContent
import market.engine.fragments.root.main.createOrder.DefaultCreateOrderComponent
import market.engine.fragments.root.main.createSubscription.CreateSubscriptionComponent
import market.engine.fragments.root.main.createSubscription.CreateSubscriptionContent
import market.engine.fragments.root.main.createSubscription.DefaultCreateSubscriptionComponent
import market.engine.fragments.root.main.listing.DefaultListingComponent
import market.engine.fragments.root.main.listing.ListingComponent
import market.engine.fragments.root.main.listing.ListingContent
import market.engine.fragments.root.main.messenger.DefaultDialogsComponent
import market.engine.fragments.root.main.messenger.DialogsComponent
import market.engine.fragments.root.main.messenger.DialogsContent
import market.engine.fragments.root.main.offer.DefaultOfferComponent
import market.engine.fragments.root.main.offer.OfferComponent
import market.engine.fragments.root.main.offer.OfferContent
import market.engine.fragments.root.main.profile.ProfileConfig.CreateOfferScreen
import market.engine.fragments.root.main.profile.ProfileConfig.CreateOrderScreen
import market.engine.fragments.root.main.profile.ProfileConfig.DialogsScreen
import market.engine.fragments.root.main.profile.ProfileConfig.ListingScreen
import market.engine.fragments.root.main.profile.ProfileConfig.OfferScreen
import market.engine.fragments.root.main.profile.ProfileConfig.ProposalScreen
import market.engine.fragments.root.main.profile.ProfileConfig.UserScreen
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.profile.conversations.ConversationsComponent
import market.engine.fragments.root.main.profile.conversations.ConversationsContent
import market.engine.fragments.root.main.profile.conversations.DefaultConversationsComponent
import market.engine.fragments.root.main.profile.myBids.ProfileMyBidsNavigation
import market.engine.fragments.root.main.profile.myOffers.ProfileMyOffersNavigation
import market.engine.fragments.root.main.profile.myOrders.ProfileMyOrdersNavigation
import market.engine.fragments.root.main.profile.myProposals.ProfileMyProposalsNavigation
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsNavigation
import market.engine.fragments.root.main.proposalPage.DefaultProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalContent
import market.engine.fragments.root.main.user.DefaultUserComponent
import market.engine.fragments.root.main.user.UserContent

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
    data class ConversationsScreen(val message : String? = null) : ProfileConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : ProfileConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD, val ts : String?) : ProfileConfig()

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
    data class DialogsScreen(val dialogId : Long, val message: String? = null, val ts: String) : ProfileConfig()
    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType, val ts: String) : ProfileConfig()

    @Serializable
    data class CreateSubscriptionScreen(
        val editId : Long? = null,
    ) : ProfileConfig()
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
    class CreateSubscriptionChild(val component: CreateSubscriptionComponent) : ChildProfile()
}

@Composable
fun ProfileNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildProfile>>,
    publicProfileNavigationItems: List<NavigationItem>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = backAnimation(
            backHandler = when (val screen = stack.active.instance) {
                is ChildProfile.ListingChild -> screen.component.model.value.backHandler
                is ChildProfile.OfferChild -> screen.component.model.value.backHandler
                is ChildProfile.UserChild -> screen.component.model.value.backHandler
                is ChildProfile.CreateOfferChild -> screen.component.model.value.backHandler
                is ChildProfile.CreateOrderChild -> screen.component.model.value.backHandler
                is ChildProfile.DialogsChild -> screen.component.model.value.backHandler
                is ChildProfile.ProposalChild -> screen.component.model.value.backHandler
                is ChildProfile.CreateSubscriptionChild -> screen.component.model.value.backHandler
                is ChildProfile.ConversationsChild -> screen.component.model.value.backHandler
                is ChildProfile.MyBidsChild -> screen.component.model.value.backHandler
                is ChildProfile.MyOffersChild -> screen.component.model.value.backHandler
                is ChildProfile.MyOrdersChild -> screen.component.model.value.backHandler
                is ChildProfile.MyProposalsChild -> screen.component.model.value.backHandler
                is ChildProfile.ProfileChild -> screen.component.model.value.backHandler
                is ChildProfile.ProfileSettingsChild -> screen.component.model.value.backHandler
            },
            onBack = {
                when (val screen = stack.active.instance) {
                    is ChildProfile.ListingChild -> screen.component.goBack()
                    is ChildProfile.OfferChild -> screen.component.onBackClick()
                    is ChildProfile.UserChild -> screen.component.onBack()
                    is ChildProfile.CreateOfferChild -> screen.component.onBackClicked()
                    is ChildProfile.CreateOrderChild -> screen.component.onBackClicked()
                    is ChildProfile.DialogsChild -> screen.component.onBackClicked()
                    is ChildProfile.ProposalChild -> screen.component.goBack()
                    is ChildProfile.CreateSubscriptionChild -> screen.component.onBackClicked()
                    is ChildProfile.ConversationsChild -> screen.component.onBack()
                    is ChildProfile.MyBidsChild -> screen.component.onBack()
                    is ChildProfile.MyOffersChild -> screen.component.onBack()
                    is ChildProfile.MyOrdersChild -> screen.component.onBack()
                    is ChildProfile.MyProposalsChild -> screen.component.onBack()
                    is ChildProfile.ProfileSettingsChild -> screen.component.onBack()
                    is ChildProfile.ProfileChild -> {}
                }
            }
        ),
    ) { child ->
        when (val screen = child.instance) {
            is ChildProfile.ProfileChild -> ProfileContent(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.MyOffersChild -> ProfileMyOffersNavigation(
                screen.component,
                modifier,
                publicProfileNavigationItems
            )
            is ChildProfile.OfferChild -> OfferContent(screen.component)
            is ChildProfile.ListingChild -> ListingContent(screen.component, modifier)
            is ChildProfile.UserChild -> UserContent(screen.component, modifier)
            is ChildProfile.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildProfile.CreateOrderChild -> CreateOrderContent(screen.component)
            is ChildProfile.MyOrdersChild -> ProfileMyOrdersNavigation(
                screen.type,
                screen.component,
                modifier,
                publicProfileNavigationItems
            )
            is ChildProfile.ConversationsChild -> ConversationsContent(screen.component, modifier, publicProfileNavigationItems)
            is ChildProfile.MyBidsChild -> ProfileMyBidsNavigation(
                screen.component,
                modifier,
                publicProfileNavigationItems
            )
            is ChildProfile.DialogsChild -> DialogsContent(screen.component, modifier)
            is ChildProfile.ProfileSettingsChild -> ProfileSettingsNavigation(
                screen.component,
                modifier,
                publicProfileNavigationItems
            )
            is ChildProfile.MyProposalsChild -> ProfileMyProposalsNavigation(
                screen.component,
                modifier,
                publicProfileNavigationItems
            )
            is ChildProfile.ProposalChild -> ProposalContent(screen.component)
            is ChildProfile.CreateSubscriptionChild -> CreateSubscriptionContent(screen.component)
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
fun createProfileChild(
    config: ProfileConfig,
    componentContext: JetpackComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToSubscribe: () -> Unit
): ChildProfile {
    return when (config) {
        is ProfileConfig.ProfileScreen -> ChildProfile.ProfileChild(
            DefaultProfileComponent(
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

        is OfferScreen -> ChildProfile.OfferChild(
            component =

                DefaultOfferComponent(
                    config.id,
                    config.isSnapshot,
                    componentContext,
                    selectOffer = { newId->
                        profileNavigation.pushNew(
                            OfferScreen(newId, getCurrentDate())
                        )
                    },
                    navigationBack = {
                        profileNavigation.pop()
                    },
                    navigationListing = {
                        profileNavigation.pushNew(
                            ListingScreen(it.data, it.searchData, getCurrentDate())
                        )
                    },
                    navigateToUser = { ui, about ->
                        profileNavigation.pushNew(
                            UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    navigationCreateOffer = { type, catPath, offerId, externalImages ->
                        profileNavigation.pushNew(
                            CreateOfferScreen(
                                catPath = catPath,
                                createOfferType = type,
                                externalImages = externalImages,
                                offerId = offerId
                            )
                        )
                    },
                    navigateToCreateOrder = { item ->
                        profileNavigation.pushNew(
                            CreateOrderScreen(item)
                        )
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDialog = { dialogId ->
                        if(dialogId != null)
                            profileNavigation.pushNew(DialogsScreen(dialogId, null, getCurrentDate()))
                        else
                            profileNavigation.replaceAll(ProfileConfig.ConversationsScreen())
                    },
                    navigationSubscribes = {
                        navigateToSubscribe()
                    },
                    navigateToProposalPage = { offerId, type ->
                        profileNavigation.pushNew(
                            ProposalScreen(offerId, type, getCurrentDate())
                        )
                    },
                    navigateDynamicSettings = { type, owner ->
                        goToDynamicSettings(type, owner, null)
                    }
                )
        )

        is ListingScreen -> {
            val ld = ListingData(
                searchData = config.searchData,
                data = config.listingData
            )
            ChildProfile.ListingChild(
                component = DefaultListingComponent(
                    componentContext = componentContext,
                    listingData = ld,
                    selectOffer = {
                        profileNavigation.pushNew(
                            OfferScreen(it, getCurrentDate())
                        )
                    },
                    selectedBack = {
                        profileNavigation.pop()
                    },
                    navigateToSubscribe = {
                        navigateToSubscribe()
                    },
                    navigateToListing = {
                        profileNavigation.pushNew(
                            ListingScreen(it.data, it.searchData, getCurrentDate())
                        )
                    },
                    navigateToNewSubscription = {
                        profileNavigation.pushNew(
                            ProfileConfig.CreateSubscriptionScreen(it)
                        )
                    },
                    isOpenSearch = false,
                )
            )
        }

        is UserScreen -> {
            ChildProfile.UserChild(
                component =
                    DefaultUserComponent(
                        userId = config.userId,
                        isClickedAboutMe = config.aboutMe,
                        componentContext = componentContext,
                        goToListing = {
                            profileNavigation.pushNew(
                                ListingScreen(it.data, it.searchData, getCurrentDate())
                            )
                        },
                        navigateBack = {
                            profileNavigation.pop()
                        },
                        navigateToOrder = { id, type ->
                            navigateToMyOrders(id, type)
                        },
                        navigateToSnapshot = { id ->
                            profileNavigation.pushNew(
                                OfferScreen(id, getCurrentDate(), true)
                            )
                        },
                        navigateToUser = {
                            profileNavigation.pushNew(
                                UserScreen(it, getCurrentDate(), false)
                            )
                        },
                        navigateToSubscriptions = {
                            navigateToSubscribe()
                        },
                    )
            )
        }

        is CreateOfferScreen -> ChildProfile.CreateOfferChild(
            component =
                DefaultCreateOfferComponent(
                    catPath = config.catPath,
                    offerId = config.offerId,
                    type = config.createOfferType,
                    externalImages = config.externalImages,
                    componentContext,
                    navigateToOffer = { id->
                        profileNavigation.pushNew(
                            OfferScreen(id, getCurrentDate())
                        )
                    },
                    navigateToCreateOffer = { id, path, t ->
                        profileNavigation.replaceCurrent(
                            CreateOfferScreen(
                                catPath = path,
                                offerId = id,
                                createOfferType = t,
                            )
                        )
                    },
                    navigateBack = {
                        profileNavigation.pop()
                    }
                )
        )

        is CreateOrderScreen -> ChildProfile.CreateOrderChild(
            component = DefaultCreateOrderComponent(
                componentContext,
                config.basketItem,
                navigateToOffer = { id->
                    profileNavigation.pushNew(
                        OfferScreen(id, getCurrentDate())
                    )
                },
                navigateBack = {
                    profileNavigation.pop()
                },
                navigateToUser = { id->
                    profileNavigation.pushNew(
                        UserScreen(id, getCurrentDate(), false)
                    )
                },
                navigateToMyOrders = {
                    profileNavigation.pop()
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
            component = DefaultConversationsComponent(
                config.message,
                componentContext,
                navigateBack = {
                    profileNavigation.replaceAll(ProfileConfig.ProfileScreen())
                },
                navigateToMessenger = { id, message ->
                    profileNavigation.pushNew(
                        DialogsScreen(id,message, getCurrentDate())
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

        is DialogsScreen -> ChildProfile.DialogsChild(
            DefaultDialogsComponent(
                componentContext = componentContext,
                dialogId = config.dialogId,
                message = config.message,
                navigateBack = {
                    profileNavigation.pop()
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                },
                navigateToUser = {
                    profileNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToOffer = {
                    profileNavigation.pushNew(
                        OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToListingSelected = {
                    profileNavigation.pushNew(
                        ListingScreen(it.data, it.searchData, getCurrentDate())
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

        is ProposalScreen -> ChildProfile.ProposalChild(
            component = DefaultProposalComponent(
                offerId = config.offerId,
                proposalType = config.proposalType,
                componentContext = componentContext,
                navigateToOffer = {
                    profileNavigation.pushNew(
                        OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToUser = {
                    profileNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateBack = {
                    profileNavigation.pop()
                }
            )
        )

        is ProfileConfig.CreateSubscriptionScreen -> {
            ChildProfile.CreateSubscriptionChild(
                component =
                    DefaultCreateSubscriptionComponent(
                        componentContext,
                        config.editId,
                        navigateBack = {
                            profileNavigation.pop()
                        },
                    )
            )
        }
    }
}
