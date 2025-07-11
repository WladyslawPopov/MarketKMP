package market.engine.fragments.root.main.home

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
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.UserData
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.DefaultRootComponent.Companion.goToContactUs
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
import market.engine.fragments.root.main.home.ChildHome.*
import market.engine.fragments.root.main.home.HomeConfig.*
import market.engine.fragments.root.main.listing.DefaultListingComponent
import market.engine.fragments.root.main.listing.ListingComponent
import market.engine.fragments.root.main.listing.ListingContent
import market.engine.fragments.root.main.messenger.DefaultDialogsComponent
import market.engine.fragments.root.main.messenger.DialogsComponent
import market.engine.fragments.root.main.messenger.DialogsContent
import market.engine.fragments.root.main.notificationsHistory.DefaultNotificationsHistoryComponent
import market.engine.fragments.root.main.notificationsHistory.NotificationsHistoryComponent
import market.engine.fragments.root.main.notificationsHistory.NotificationsHistoryContent
import market.engine.fragments.root.main.offer.DefaultOfferComponent
import market.engine.fragments.root.main.offer.OfferComponent
import market.engine.fragments.root.main.offer.OfferContent
import market.engine.fragments.root.main.proposalPage.ProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalContent
import market.engine.fragments.root.main.proposalPage.proposalFactory
import market.engine.fragments.root.main.user.DefaultUserComponent
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent

@Serializable
sealed class HomeConfig {
    @Serializable
    data object HomeScreen : HomeConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : HomeConfig()

    @Serializable
    data class ListingScreen(val isOpenSearch : Boolean, val listingData: LD, val searchData : SD, val ts : String?) : HomeConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe : Boolean) : HomeConfig()

    @Serializable
    data class CreateOfferScreen(
        val catPath: List<Long>? = null,
        val offerId: Long? = null,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>? = null
    ) : HomeConfig()

    @Serializable
    data class CreateOrderScreen(
        val basketItem : Pair<Long, List<SelectedBasketItem>>,
    ) : HomeConfig()

    @Serializable
    data class MessagesScreen(
        val dialogId: Long,
        val text: String? = null,
        val ts: String
    ) : HomeConfig()

    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType, val ts: String?) : HomeConfig()

    @Serializable
    data class CreateSubscriptionScreen(
        val editId : Long? = null,
    ) : HomeConfig()
    
    @Serializable
    data object NotificationHistoryScreen : HomeConfig()
}

sealed class ChildHome {
    class HomeChild(val component: HomeComponent) : ChildHome()
    class OfferChild(val component: OfferComponent) : ChildHome()
    class ListingChild(val component: ListingComponent) : ChildHome()
    class UserChild(val component: UserComponent) : ChildHome()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildHome()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildHome()
    class MessagesChild(val component: DialogsComponent) : ChildHome()
    class ProposalChild(val component: ProposalComponent) : ChildHome()
    class CreateSubscriptionChild(val component: CreateSubscriptionComponent) : ChildHome()
    class NotificationHistoryChild(val component: NotificationsHistoryComponent) : ChildHome()
}

@Composable
fun HomeNavigation (
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildHome>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is HomeChild ->{
                HomeContent(screen.component, modifier)
            }
            is OfferChild ->{
                OfferContent(screen.component, modifier)
            }
            is ListingChild ->{
                ListingContent(screen.component, modifier)
            }
            is UserChild ->{
                UserContent(screen.component, modifier)
            }
            is CreateOfferChild ->{
                CreateOfferContent(screen.component)
            }
            is CreateOrderChild -> {
                CreateOrderContent(screen.component)
            }
            is MessagesChild -> {
                DialogsContent(screen.component, modifier)
            }
            is ProposalChild -> {
                ProposalContent(screen.component)
            }
            is CreateSubscriptionChild -> {
                CreateSubscriptionContent(screen.component)
            }
            is NotificationHistoryChild -> {
                NotificationsHistoryContent(screen.component)
            }
        }
    }
}

fun createHomeChild(
    config: HomeConfig,
    componentContext: ComponentContext,
    homeNavigation: StackNavigation<HomeConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToConversations: () -> Unit,
    navigateToSubscribe: () -> Unit,
    navigateToMyProposals: () -> Unit,
    navigateToDeepLink: (DeepLink) -> Unit,
): ChildHome =
    when (config) {
        HomeScreen -> HomeChild(
            DefaultHomeComponent(
                componentContext = componentContext,
                navigateToListingSelected = { ld, isNewSearch ->
                    homeNavigation.pushNew(
                        ListingScreen(
                            isNewSearch,
                            ld.data,
                            ld.searchData,
                            getCurrentDate()
                        )
                    )
                },
                navigateToLoginSelected = {
                    goToLogin(true)
                },
                navigateToOfferSelected = { id ->
                    homeNavigation.pushNew(OfferScreen(id, getCurrentDate()))
                },
                navigateToCreateOfferSelected = {
                    if (UserData.token != "") {
                        homeNavigation.pushNew(
                            CreateOfferScreen(
                                null,
                                null,
                                CreateOfferType.CREATE,
                                null
                            )
                        )
                    } else {
                        goToLogin(true)
                    }
                },
                navigateToMessengerSelected = {
                    navigateToConversations()
                },
                navigateToContactUsSelected = {
                    goToContactUs(null)
                },
                navigateToSettingsSelected = {
                    goToDynamicSettings("app_settings", null, null)
                },
                navigateToMyProposalsSelected = {
                    navigateToMyProposals()
                },
                navigateToNotificationHistorySelected = {
                    homeNavigation.pushNew(
                        NotificationHistoryScreen
                    )
                }
            )
        )

        is OfferScreen -> OfferChild(
            component =
                DefaultOfferComponent(
                    config.id,
                    config.isSnapshot,
                    componentContext,
                    selectOffer = { newId->
                        homeNavigation.pushNew(
                            OfferScreen(newId, getCurrentDate())
                        )
                    },
                    navigationBack = {
                        homeNavigation.pop()
                    },
                    navigationListing = {
                        homeNavigation.pushNew(
                            ListingScreen(false, it.data, it.searchData, getCurrentDate())
                        )
                    },
                    navigateToUser = { ui, about ->
                        homeNavigation.pushNew(
                            UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    navigationCreateOffer = { type, catPath, offerId, externalImages ->
                        homeNavigation.pushNew(
                            CreateOfferScreen(
                                catPath = catPath,
                                createOfferType = type,
                                externalImages = externalImages,
                                offerId = offerId
                            )
                        )
                    },
                    navigateToCreateOrder = { item ->
                        homeNavigation.pushNew(
                            CreateOrderScreen(item)
                        )
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDialog = { dialogId ->
                        if(dialogId != null)
                            homeNavigation.pushNew(MessagesScreen(dialogId, null, getCurrentDate()))
                        else
                            navigateToConversations()
                    },
                    navigationSubscribes = {
                        navigateToSubscribe()
                    },
                    navigateToProposalPage = { offerId, type ->
                        homeNavigation.pushNew(
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
            ListingChild(
                component = DefaultListingComponent(
                    componentContext = componentContext,
                    listingData = ld,
                    selectOffer = {
                        homeNavigation.pushNew(
                            OfferScreen(it, getCurrentDate())
                        )
                    },
                    selectedBack = {
                        homeNavigation.pop()
                    },
                    isOpenSearch = config.isOpenSearch,
                    navigateToSubscribe = {
                        navigateToSubscribe()
                    },
                    navigateToListing = {
                        homeNavigation.pushNew(
                            ListingScreen(false, it.data, it.searchData, getCurrentDate())
                        )
                    },
                    navigateToNewSubscription = {
                        homeNavigation.pushNew(
                            CreateSubscriptionScreen(it)
                        )
                    }
                ),
            )
        }

        is UserScreen -> UserChild(
            component = DefaultUserComponent(
                userId = config.userId,
                isClickedAboutMe = config.aboutMe,
                componentContext = componentContext,
                goToListing = {
                    homeNavigation.pushNew(
                        ListingScreen(false, it.data, it.searchData, getCurrentDate())
                    )
                },
                navigateBack = {
                    homeNavigation.pop()
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                },
                navigateToSnapshot = { id ->
                    homeNavigation.pushNew(
                        OfferScreen(id, getCurrentDate(), true)
                    )
                },
                navigateToUser = {
                    homeNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToSubscriptions = {
                    navigateToSubscribe()
                },
            )
        )

        is CreateOfferScreen -> CreateOfferChild(
            component =
                DefaultCreateOfferComponent(
                    catPath = config.catPath,
                    offerId = config.offerId,
                    type = config.createOfferType,
                    externalImages = config.externalImages,
                    componentContext,
                    navigateToOffer = { id->
                        homeNavigation.pushNew(
                            OfferScreen(id, getCurrentDate())
                        )
                    },
                    navigateToCreateOffer = { id, path, t ->
                        homeNavigation.replaceCurrent(
                            CreateOfferScreen(
                                catPath = path,
                                offerId = id,
                                createOfferType = t,
                            )
                        )
                    },
                    navigateBack = {
                        homeNavigation.pop()
                    }
                )
        )

        is CreateOrderScreen -> CreateOrderChild(
            component = DefaultCreateOrderComponent(
                componentContext,
                config.basketItem,
                navigateToOffer = { id->
                    homeNavigation.pushNew(
                        OfferScreen(id, getCurrentDate())
                    )
                },
                navigateBack = {
                    homeNavigation.pop()
                },
                navigateToUser = { id->
                    homeNavigation.pushNew(
                        UserScreen(id, getCurrentDate(), false)
                    )
                },
                navigateToMyOrders = {
                    homeNavigation.pop()
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

        is MessagesScreen -> MessagesChild(
            component =
                DefaultDialogsComponent(
                    componentContext = componentContext,
                    dialogId = config.dialogId,
                    message = config.text,
                    navigateBack = {
                        homeNavigation.pop()
                    },
                    navigateToOrder = { id, type ->
                        navigateToMyOrders(id, type)
                    },
                    navigateToUser = {
                        homeNavigation.pushNew(
                            UserScreen(it, getCurrentDate(), false)
                        )
                    },
                    navigateToOffer = {
                        homeNavigation.pushNew(
                            OfferScreen(it, getCurrentDate())
                        )
                    },
                    navigateToListingSelected = {
                        homeNavigation.pushNew(
                            ListingScreen(false, it.data, it.searchData, getCurrentDate())
                        )
                    }
                )
        )

        is ProposalScreen -> ProposalChild(
            component = proposalFactory(
                componentContext = componentContext,
                offerId = config.offerId,
                proposalType = config.proposalType,
                navigateBack = {
                    homeNavigation.pop()
                },
                navigateToOffer = {
                    homeNavigation.pop()
                    homeNavigation.pushNew(
                        OfferScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToUser = {
                    homeNavigation.pop()
                    homeNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                }
            )
        )

        is CreateSubscriptionScreen -> {
            CreateSubscriptionChild(
                component = DefaultCreateSubscriptionComponent(
                    componentContext,
                    config.editId,
                    navigateBack = {
                        homeNavigation.pop()
                    },
                )
            )
        }

        NotificationHistoryScreen -> {
            NotificationHistoryChild(
                DefaultNotificationsHistoryComponent(
                    componentContext = componentContext,
                    navigateBack = {
                        homeNavigation.pop()
                    },
                    navigateDeepLink = {
                        navigateToDeepLink(it)
                    }
                )
            )
        }
    }
