package market.engine.fragments.root.main.favPages

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
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.fragments.root.main.user.userFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.createOffer.CreateOfferComponent
import market.engine.fragments.root.main.createOffer.CreateOfferContent
import market.engine.fragments.root.main.createOffer.createOfferFactory
import market.engine.fragments.root.main.createOrder.CreateOrderComponent
import market.engine.fragments.root.main.createOrder.CreateOrderContent
import market.engine.fragments.root.main.createOrder.createOrderFactory
import market.engine.fragments.root.main.createSubscription.CreateSubscriptionComponent
import market.engine.fragments.root.main.createSubscription.CreateSubscriptionContent
import market.engine.fragments.root.main.createSubscription.createSubscriptionFactory
import market.engine.fragments.root.main.listing.ListingComponent
import market.engine.fragments.root.main.listing.ListingContent
import market.engine.fragments.root.main.listing.listingFactory
import market.engine.fragments.root.main.messenger.DialogsComponent
import market.engine.fragments.root.main.messenger.DialogsContent
import market.engine.fragments.root.main.messenger.messengerFactory
import market.engine.fragments.root.main.offer.OfferComponent
import market.engine.fragments.root.main.offer.OfferContent
import market.engine.fragments.root.main.offer.offerFactory
import market.engine.fragments.root.main.proposalPage.ProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalContent
import market.engine.fragments.root.main.proposalPage.proposalFactory
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent

@Serializable
sealed class FavoritesConfig {
    @Serializable
    data class FavPagesScreen(val favScreenType: FavScreenType, val ts: String? = null) : FavoritesConfig()

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
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildFavorites.FavPagesChild -> FavPagesNavigation(screen.component, modifier)
            is ChildFavorites.OfferChild -> OfferContent(screen.component, modifier)
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

fun createFavoritesChild(
    config: FavoritesConfig,
    componentContext: ComponentContext,
    favPagesViewModel: FavPagesViewModel,
    favoritesNavigation : StackNavigation<FavoritesConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToConversations: () -> Unit,
): ChildFavorites = when (config) {
        is FavoritesConfig.FavPagesScreen -> ChildFavorites.FavPagesChild(
            itemFavPages(componentContext, favPagesViewModel, favoritesNavigation, config.favScreenType)
        )

        is FavoritesConfig.OfferScreen -> ChildFavorites.OfferChild(
            component = offerFactory(
                componentContext,
                config.id,
                selectOffer = {
                    favoritesNavigation.pushNew(
                        FavoritesConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                onBack = {
                    favoritesNavigation.pop()
                },
                onListingSelected = {
                    favoritesNavigation.pushNew(
                        FavoritesConfig.ListingScreen(it.data.value, it.searchData.value, getCurrentDate())
                    )
                },
                onUserSelected = { ui, about ->
                    favoritesNavigation.pushNew(
                        FavoritesConfig.UserScreen(ui, getCurrentDate(), about)
                    )
                },
                isSnapshot = config.isSnap,
                navigateToCreateOffer = { type, catPath, offerId, externalImages ->
                    favoritesNavigation.pushNew(
                        FavoritesConfig.CreateOfferScreen(
                            catPath = catPath,
                            createOfferType = type,
                            externalImages = externalImages,
                            offerId = offerId
                        )
                    )
                },
                navigateToCreateOrder = { item ->
                    favoritesNavigation.pushNew(
                        FavoritesConfig.CreateOrderScreen(item)
                    )
                },
                navigateToLogin = {
                    goToLogin(true)
                },
                navigateToDialog = { dialogId ->
                    if (dialogId != null)
                        favoritesNavigation.pushNew(FavoritesConfig.MessengerScreen(dialogId, getCurrentDate()))
                    else
                        navigateToConversations()
                },
                navigationSubscribes = {
                    favoritesNavigation.replaceAll(FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED))
                },
                navigateToProposalPage = { offerId, type ->
                    favoritesNavigation.pushNew(
                        FavoritesConfig.ProposalScreen(offerId, type, getCurrentDate())
                    )
                }
            )
        )

        is FavoritesConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = mutableStateOf(config.searchData),
                data = mutableStateOf(config.listingData)
            )
            ChildFavorites.ListingChild(
                component = listingFactory(
                    componentContext,
                    ld,
                    selectOffer = {
                        favoritesNavigation.pushNew(
                            FavoritesConfig.OfferScreen(it, getCurrentDate())
                        )
                    },
                    onBack = {
                        favoritesNavigation.pop()
                    },
                    isOpenCategory = false,
                    navigateToSubscribe = {
                        favoritesNavigation.replaceAll(FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED))
                    },
                    navigateToListing = { data ->
                        favoritesNavigation.pushNew(
                            FavoritesConfig.ListingScreen(data.data.value, data.searchData.value, getCurrentDate())
                        )
                    },
                    navigateToNewSubscription = {
                        favoritesNavigation.pushNew(
                            FavoritesConfig.CreateSubscriptionScreen(it)
                        )
                    }
                )
            )
        }

        is FavoritesConfig.UserScreen -> ChildFavorites.UserChild(
            userFactory(
                componentContext,
                config.userId,
                config.aboutMe,
                goToLogin = {
                    favoritesNavigation.pushNew(
                        FavoritesConfig.ListingScreen(it.data.value, it.searchData.value, getCurrentDate())
                    )
                },
                goBack = {
                    favoritesNavigation.pop()
                },
                goToSnapshot = { id ->
                    favoritesNavigation.pushNew(
                        FavoritesConfig.OfferScreen(id, getCurrentDate(), true)
                    )
                },
                goToUser = {
                    favoritesNavigation.pushNew(
                        FavoritesConfig.UserScreen(it, getCurrentDate(), false)
                    )
                },
                goToSubscriptions = {
                    favoritesNavigation.replaceAll(FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED))
                },
                goToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                }
            )
        )

        is FavoritesConfig.CreateOfferScreen -> ChildFavorites.CreateOfferChild(
            component = createOfferFactory(
                componentContext = componentContext,
                catPath = config.catPath,
                offerId = config.offerId,
                type = config.createOfferType,
                externalImages = config.externalImages,
                navigateOffer = { id ->
                    favoritesNavigation.pushNew(
                        FavoritesConfig.OfferScreen(id, getCurrentDate())
                    )
                },
                navigateCreateOffer = { id, path, t ->
                    favoritesNavigation.replaceCurrent(
                        FavoritesConfig.CreateOfferScreen(
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

        is FavoritesConfig.CreateOrderScreen -> ChildFavorites.CreateOrderChild(
            component = createOrderFactory(
                componentContext = componentContext,
                selectedItems = config.basketItem,
                navigateUser = {
                    favoritesNavigation.pushNew(
                        FavoritesConfig.UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateOffer = {
                    favoritesNavigation.pushNew(
                        FavoritesConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateBack = {
                    favoritesNavigation.pop()
                },
                navigateToMyOrders = {
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

    is FavoritesConfig.CreateSubscriptionScreen -> ChildFavorites.CreateSubscriptionChild(
        createSubscriptionFactory(
            componentContext = componentContext,
            editId = config.editId,
            navigateBack = {
                favoritesNavigation.pop()
            }
        )
    )

    is FavoritesConfig.MessengerScreen -> ChildFavorites.MessengerChild(
        component = messengerFactory(
            componentContext = componentContext,
            dialogId = config.dialogId,
            navigateBack = {
                favoritesNavigation.pop()
            },
            navigateToOrder = { id, type ->
                navigateToMyOrders(id, type)
            },
            navigateToUser = {
                favoritesNavigation.pushNew(
                    FavoritesConfig.UserScreen(it, getCurrentDate(), false)
                )
            },
            navigateToOffer = {
                favoritesNavigation.pushNew(
                    FavoritesConfig.OfferScreen(it, getCurrentDate())
                )
            },
            navigateToListingSelected = {
                favoritesNavigation.pushNew(
                    FavoritesConfig.ListingScreen(it.data.value, it.searchData.value, getCurrentDate())
                )
            }
        )
    )

    is FavoritesConfig.ProposalScreen -> ChildFavorites.ProposalChild(
        component = proposalFactory(
            componentContext = componentContext,
            offerId = config.offerId,
            proposalType = config.proposalType,
            navigateBack = {
                favoritesNavigation.pop()
            },
            navigateToOffer = {
                favoritesNavigation.pushNew(
                    FavoritesConfig.OfferScreen(it, getCurrentDate())
                )
            },
            navigateToUser = {
                favoritesNavigation.pushNew(
                    FavoritesConfig.UserScreen(it, getCurrentDate(), false)
                )
            }
        )
    )
}

fun itemFavPages(
    componentContext: ComponentContext,
    favPagesViewModel: FavPagesViewModel,
    favoritesNavigation : StackNavigation<FavoritesConfig>,
    favType: FavScreenType = FavScreenType.FAVORITES
): FavPagesComponent {
    return DefaultFavPagesComponent(
        favoritesNavigation = favoritesNavigation,
        componentContext = componentContext,
        viewModel = favPagesViewModel,
        favType = favType
    )
}
