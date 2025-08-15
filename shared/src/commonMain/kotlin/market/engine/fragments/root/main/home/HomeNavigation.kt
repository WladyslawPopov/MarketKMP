package market.engine.fragments.root.main.home

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
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.common.backAnimation
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.UserData
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.nowAsEpochSeconds
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
import market.engine.fragments.root.main.proposalPage.DefaultProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalContent
import market.engine.fragments.root.main.user.DefaultUserComponent
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent

@Serializable
sealed class HomeConfig {
    @Serializable
    data object HomeScreen : HomeConfig()

    @Serializable
    data class OfferScreen(val id: Long,val ts: Long?, val isSnapshot: Boolean = false) : HomeConfig()

    @Serializable
    data class ListingScreen(val isOpenSearch : Boolean, val listingData: LD, val searchData : SD, val ts: Long?) : HomeConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: Long?, val aboutMe : Boolean) : HomeConfig()

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
        val ts: Long?
    ) : HomeConfig()

    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType, val ts: Long?) : HomeConfig()

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
        animation = backAnimation(
            backHandler = when (val screen = stack.active.instance) {
                is HomeChild ->{
                    screen.component.model.value.backHandler
                }
                is OfferChild ->{
                    screen.component.model.value.backHandler
                }
                is ListingChild ->{
                    screen.component.model.value.backHandler
                }
                is UserChild ->{
                    screen.component.model.value.backHandler
                }
                is CreateOfferChild ->{
                    screen.component.model.value.backHandler
                }
                is CreateOrderChild -> {
                    screen.component.model.value.backHandler
                }
                is MessagesChild -> {
                    screen.component.model.value.backHandler
                }
                is ProposalChild -> {
                    screen.component.model.value.backHandler
                }
                is CreateSubscriptionChild -> {
                    screen.component.model.value.backHandler
                }
                is NotificationHistoryChild -> {
                    screen.component.model.value.backHandler
                }
            },
            onBack = {
                when (val screen = stack.active.instance) {
                    is HomeChild -> {

                    }

                    is OfferChild -> {
                        screen.component.onBackClick()
                    }

                    is ListingChild -> {
                        screen.component.goBack()
                    }

                    is UserChild -> {
                        screen.component.onBackClick()
                    }

                    is CreateOfferChild -> {
                        screen.component.onBackClicked()
                    }

                    is CreateOrderChild -> {
                        screen.component.onBackClicked()
                    }

                    is MessagesChild -> {
                        screen.component.onBackClicked()
                    }

                    is ProposalChild -> {
                        screen.component.goBack()
                    }

                    is CreateSubscriptionChild -> {
                        screen.component.onBackClicked()
                    }

                    is NotificationHistoryChild -> {
                        screen.component.onBackClicked()
                    }
                }
            }
        ),
    ) { child ->
        when (val screen = child.instance) {
            is HomeChild ->{
                HomeContent(screen.component, modifier)
            }
            is OfferChild ->{
                OfferContent(screen.component)
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

@OptIn(ExperimentalDecomposeApi::class)
fun createHomeChild(
    config: HomeConfig,
    componentContext: JetpackComponentContext,
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
                            nowAsEpochSeconds()
                        )
                    )
                },
                navigateToLoginSelected = {
                    goToLogin()
                },
                navigateToOfferSelected = { id ->
                    homeNavigation.pushNew(OfferScreen(id, nowAsEpochSeconds()))
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
                        goToLogin()
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
                            OfferScreen(newId, nowAsEpochSeconds())
                        )
                    },
                    navigationBack = {
                        homeNavigation.pop()
                    },
                    navigationListing = {
                        homeNavigation.pushNew(
                            ListingScreen(false, it.data, it.searchData, nowAsEpochSeconds())
                        )
                    },
                    navigateToUser = { ui, about ->
                        homeNavigation.pushNew(
                            UserScreen(ui, nowAsEpochSeconds(), about)
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
                        goToLogin()
                    },
                    navigateToDialog = { dialogId ->
                        if(dialogId != null)
                            homeNavigation.pushNew(MessagesScreen(dialogId, null, nowAsEpochSeconds()))
                        else
                            navigateToConversations()
                    },
                    navigationSubscribes = {
                        navigateToSubscribe()
                    },
                    navigateToProposalPage = { offerId, type ->
                        homeNavigation.pushNew(
                            ProposalScreen(offerId, type, nowAsEpochSeconds())
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
                            OfferScreen(it, nowAsEpochSeconds())
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
                            ListingScreen(false, it.data, it.searchData, nowAsEpochSeconds())
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
                        ListingScreen(false, it.data, it.searchData, nowAsEpochSeconds())
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
                        OfferScreen(id, nowAsEpochSeconds(), true)
                    )
                },
                navigateToUser = {
                    homeNavigation.pushNew(
                        UserScreen(it, nowAsEpochSeconds(), false)
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
                            OfferScreen(id, nowAsEpochSeconds())
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
                        OfferScreen(id, nowAsEpochSeconds())
                    )
                },
                navigateBack = {
                    homeNavigation.pop()
                },
                navigateToUser = { id->
                    homeNavigation.pushNew(
                        UserScreen(id, nowAsEpochSeconds(), false)
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
                            UserScreen(it, nowAsEpochSeconds(), false)
                        )
                    },
                    navigateToOffer = {
                        homeNavigation.pushNew(
                            OfferScreen(it, nowAsEpochSeconds())
                        )
                    },
                    navigateToListingSelected = {
                        homeNavigation.pushNew(
                            ListingScreen(false, it.data, it.searchData, nowAsEpochSeconds())
                        )
                    }
                )
        )

        is ProposalScreen -> ProposalChild(
            component = DefaultProposalComponent(
                offerId = config.offerId,
                proposalType = config.proposalType,
                componentContext = componentContext,
                navigateToOffer = {
                    homeNavigation.pushNew(
                        OfferScreen(it, nowAsEpochSeconds())
                    )
                },
                navigateToUser = {
                    homeNavigation.pushNew(
                        UserScreen(it, nowAsEpochSeconds(), false)
                    )
                },
                navigateBack = {
                    homeNavigation.pop()
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
                    },
                    navigateToOffer = {
                        homeNavigation.pushNew(
                            OfferScreen(it, nowAsEpochSeconds())
                        )
                    }
                )
            )
        }
    }
