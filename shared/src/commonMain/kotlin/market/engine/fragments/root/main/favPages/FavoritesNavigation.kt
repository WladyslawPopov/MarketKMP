package market.engine.fragments.root.main.favPages

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
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.FavScreenType
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
import market.engine.fragments.root.main.favPages.FavoritesConfig.CreateOfferScreen
import market.engine.fragments.root.main.favPages.FavoritesConfig.CreateOrderScreen
import market.engine.fragments.root.main.favPages.FavoritesConfig.ListingScreen
import market.engine.fragments.root.main.favPages.FavoritesConfig.MessengerScreen
import market.engine.fragments.root.main.favPages.FavoritesConfig.OfferScreen
import market.engine.fragments.root.main.favPages.FavoritesConfig.ProposalScreen
import market.engine.fragments.root.main.favPages.FavoritesConfig.UserScreen
import market.engine.fragments.root.main.listing.DefaultListingComponent
import market.engine.fragments.root.main.listing.ListingComponent
import market.engine.fragments.root.main.listing.ListingContent
import market.engine.fragments.root.main.messenger.DefaultDialogsComponent
import market.engine.fragments.root.main.messenger.DialogsComponent
import market.engine.fragments.root.main.messenger.DialogsContent
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
sealed class FavoritesConfig {
    @Serializable
    data class FavPagesScreen(val favScreenType: FavScreenType) : FavoritesConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnap: Boolean = false) : FavoritesConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD, val ts : String?) : FavoritesConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe : Boolean) : FavoritesConfig()

    @Serializable
    data class CreateOfferScreen(
        val catPath: List<Long>?,
        val offerId: Long? = null,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>? = null
    ) : FavoritesConfig()

    @Serializable
    data class CreateOrderScreen(
        val basketItem : Pair<Long, List<SelectedBasketItem>>,
    ) : FavoritesConfig()

    @Serializable
    data class CreateSubscriptionScreen(
        val editId : Long? = null,
    ) : FavoritesConfig()

    @Serializable
    data class MessengerScreen( val dialogId: Long, val ts: String) : FavoritesConfig()

    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType, val ts: String?) : FavoritesConfig()
}

sealed class ChildFavorites {
    class FavPagesChild(val component: FavPagesComponent) : ChildFavorites()
    class OfferChild(val component: OfferComponent) : ChildFavorites()
    class ListingChild(val component: ListingComponent) : ChildFavorites()
    class UserChild(val component: UserComponent) : ChildFavorites()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildFavorites()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildFavorites()
    class CreateSubscriptionChild(val component: CreateSubscriptionComponent) : ChildFavorites()
    class MessengerChild(val component: DialogsComponent) : ChildFavorites()
    class ProposalChild(val component: ProposalComponent) : ChildFavorites()
}

@Composable
fun FavoritesNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildFavorites>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = backAnimation(
            backHandler = when (val screen = stack.active.instance) {
                is ChildFavorites.ListingChild -> screen.component.model.value.backHandler
                is ChildFavorites.OfferChild -> screen.component.model.value.backHandler
                is ChildFavorites.UserChild -> screen.component.model.value.backHandler
                is ChildFavorites.CreateOfferChild -> screen.component.model.value.backHandler
                is ChildFavorites.CreateOrderChild -> screen.component.model.value.backHandler
                is ChildFavorites.MessengerChild -> screen.component.model.value.backHandler
                is ChildFavorites.ProposalChild -> screen.component.model.value.backHandler
                is ChildFavorites.CreateSubscriptionChild -> screen.component.model.value.backHandler
                is ChildFavorites.FavPagesChild -> screen.component.model.value.backHandler
            },
            onBack = {
                when (val screen = stack.active.instance) {
                    is ChildFavorites.ListingChild -> screen.component.goBack()
                    is ChildFavorites.OfferChild -> screen.component.onBackClick()
                    is ChildFavorites.UserChild -> screen.component.onBackClick()
                    is ChildFavorites.CreateOfferChild -> screen.component.onBackClicked()
                    is ChildFavorites.CreateOrderChild -> screen.component.onBackClicked()
                    is ChildFavorites.MessengerChild -> screen.component.onBackClicked()
                    is ChildFavorites.ProposalChild -> screen.component.goBack()
                    is ChildFavorites.CreateSubscriptionChild -> screen.component.onBackClicked()
                    is ChildFavorites.FavPagesChild -> {}
                }
            }
        )
    ) { child ->
        when (val screen = child.instance) {
            is ChildFavorites.FavPagesChild -> FavPagesNavigation(screen.component, modifier)
            is ChildFavorites.OfferChild -> OfferContent(screen.component)
            is ChildFavorites.ListingChild -> ListingContent(screen.component, modifier)
            is ChildFavorites.UserChild -> UserContent(screen.component, modifier)
            is ChildFavorites.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildFavorites.CreateOrderChild -> CreateOrderContent(screen.component)
            is ChildFavorites.CreateSubscriptionChild -> CreateSubscriptionContent(screen.component)
            is ChildFavorites.MessengerChild -> DialogsContent(screen.component, modifier)
            is ChildFavorites.ProposalChild -> ProposalContent(screen.component)
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
fun createFavoritesChild(
    config: FavoritesConfig,
    componentContext: JetpackComponentContext,
    favoritesNavigation : StackNavigation<FavoritesConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToConversations: () -> Unit,
): ChildFavorites = when (config) {
        is FavoritesConfig.FavPagesScreen -> ChildFavorites.FavPagesChild(
            DefaultFavPagesComponent(
                favoritesNavigation = favoritesNavigation,
                componentContext = componentContext,
                favType = config.favScreenType
            )
        )
        is OfferScreen -> ChildFavorites.OfferChild(
            component =
                DefaultOfferComponent(
                    config.id,
                    config.isSnap,
                    componentContext,
                    selectOffer = { newId->
                        favoritesNavigation.pushNew(
                            OfferScreen(newId, getCurrentDate())
                        )
                    },
                    navigationBack = {
                        favoritesNavigation.pop()
                    },
                    navigationListing = {
                        favoritesNavigation.pushNew(
                            ListingScreen(it.data, it.searchData, getCurrentDate())
                        )
                    },
                    navigateToUser = { ui, about ->
                        favoritesNavigation.pushNew(
                            UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    navigationCreateOffer = { type, catPath, offerId, externalImages ->
                        favoritesNavigation.pushNew(
                            CreateOfferScreen(
                                catPath = catPath,
                                createOfferType = type,
                                externalImages = externalImages,
                                offerId = offerId
                            )
                        )
                    },
                    navigateToCreateOrder = { item ->
                        favoritesNavigation.pushNew(
                            CreateOrderScreen(item)
                        )
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDialog = { dialogId ->
                        if(dialogId != null)
                            favoritesNavigation.pushNew(MessengerScreen(dialogId, getCurrentDate()))
                        else
                            navigateToConversations()
                    },
                    navigationSubscribes = {
                        favoritesNavigation.replaceAll(FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED))
                    },
                    navigateToProposalPage = { offerId, type ->
                        favoritesNavigation.pushNew(
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
            ChildFavorites.ListingChild(
                component = DefaultListingComponent(
                    componentContext = componentContext,
                    listingData = ld,
                    selectOffer = {
                        favoritesNavigation.pushNew(
                            OfferScreen(it, getCurrentDate())
                        )
                    },
                    selectedBack = {
                        favoritesNavigation.pop()
                    },
                    navigateToSubscribe = {
                        favoritesNavigation.replaceAll(FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED))
                    },
                    navigateToListing = { data ->
                        favoritesNavigation.pushNew(
                            ListingScreen(data.data, data.searchData, getCurrentDate())
                        )
                    },
                    navigateToNewSubscription = {
                        favoritesNavigation.pushNew(
                            FavoritesConfig.CreateSubscriptionScreen(it)
                        )
                    },
                    isOpenSearch = false
                )
            )
        }

        is UserScreen -> ChildFavorites.UserChild(
            DefaultUserComponent(
                userId = config.userId,
                isClickedAboutMe = config.aboutMe,
                componentContext = componentContext,
                goToListing = {
                    favoritesNavigation.pushNew(
                        ListingScreen(it.data, it.searchData, getCurrentDate())
                    )
                },
                navigateBack = {
                    favoritesNavigation.pop()
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                },
                navigateToSnapshot = { id ->
                    favoritesNavigation.pushNew(
                        OfferScreen(id, getCurrentDate(), true)
                    )
                },
                navigateToUser = {
                    favoritesNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToSubscriptions = {
                    favoritesNavigation.replaceAll(FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED))
                },
            )
        )

        is CreateOfferScreen -> ChildFavorites.CreateOfferChild(
            component =
                DefaultCreateOfferComponent(
                    catPath = config.catPath,
                    offerId = config.offerId,
                    type = config.createOfferType,
                    externalImages = config.externalImages,
                    componentContext,
                    navigateToOffer = { id->
                        favoritesNavigation.pushNew(
                            OfferScreen(id, getCurrentDate())
                        )
                    },
                    navigateToCreateOffer = { id, path, t ->
                        favoritesNavigation.replaceCurrent(
                            CreateOfferScreen(
                                catPath = path,
                                offerId = id,
                                createOfferType = t,
                            )
                        )
                    },
                    navigateBack = {
                        favoritesNavigation.pop()
                    }
                )
        )

        is CreateOrderScreen -> ChildFavorites.CreateOrderChild(
            component = DefaultCreateOrderComponent(
                componentContext,
                config.basketItem,
                navigateToOffer = { id->
                    favoritesNavigation.pushNew(
                        OfferScreen(id, getCurrentDate())
                    )
                },
                navigateBack = {
                    favoritesNavigation.pop()
                },
                navigateToUser = { id->
                    favoritesNavigation.pushNew(
                        UserScreen(id, getCurrentDate(), false)
                    )
                },
                navigateToMyOrders = {
                    favoritesNavigation.pop()
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

    is FavoritesConfig.CreateSubscriptionScreen -> ChildFavorites.CreateSubscriptionChild(
        DefaultCreateSubscriptionComponent(
            componentContext,
            config.editId,
            navigateBack = {
                favoritesNavigation.pop()
            },
        )
    )

    is MessengerScreen -> ChildFavorites.MessengerChild(
        component =
            DefaultDialogsComponent(
                componentContext = componentContext,
                dialogId = config.dialogId,
                message = null,
                navigateBack = {
                    favoritesNavigation.pop()
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                },
                navigateToUser = {
                    favoritesNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToOffer = {
                    favoritesNavigation.pushNew(
                        OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToListingSelected = {
                    favoritesNavigation.pushNew(
                        ListingScreen(it.data, it.searchData, getCurrentDate())
                    )
                }
            )
    )

    is ProposalScreen -> ChildFavorites.ProposalChild(
        component = DefaultProposalComponent(
            offerId = config.offerId,
            proposalType = config.proposalType,
            componentContext = componentContext,
            navigateToOffer = {
                favoritesNavigation.pushNew(
                    OfferScreen(it, getCurrentDate())
                )
            },
            navigateToUser = {
                favoritesNavigation.pushNew(
                    UserScreen(it, getCurrentDate(), false)
                )
            },
            navigateBack = {
                favoritesNavigation.pop()
            }
        )
    )
}
