package market.engine.fragments.root.main.basket

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
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.basket.BasketConfig.ListingScreen
import market.engine.fragments.root.main.basket.BasketConfig.OfferScreen
import market.engine.fragments.root.main.basket.BasketConfig.UserScreen
import market.engine.fragments.root.main.createOffer.CreateOfferComponent
import market.engine.fragments.root.main.createOffer.CreateOfferContent
import market.engine.fragments.root.main.createOffer.DefaultCreateOfferComponent
import market.engine.fragments.root.main.createOrder.CreateOrderComponent
import market.engine.fragments.root.main.createOrder.CreateOrderContent
import market.engine.fragments.root.main.createOrder.createOrderFactory
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
import market.engine.fragments.root.main.proposalPage.ProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalContent
import market.engine.fragments.root.main.proposalPage.proposalFactory
import market.engine.fragments.root.main.user.DefaultUserComponent
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent

@Serializable
sealed class BasketConfig {
    @Serializable
    data object BasketScreen : BasketConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD, val ts : String?) : BasketConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnap: Boolean = false) : BasketConfig()
    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe : Boolean) : BasketConfig()

    @Serializable
    data class CreateOfferScreen(
        val catPath: List<Long>?,
        val offerId: Long? = null,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>? = null
    ) : BasketConfig()

    @Serializable
    data class CreateOrderScreen(
        val basketItem : Pair<Long, List<SelectedBasketItem>>,
    ) : BasketConfig()

    @Serializable
    data class MessengerScreen(val id: Long, val ts: String) : BasketConfig()

    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType, val ts: String?) : BasketConfig()

    @Serializable
    data class CreateSubscriptionScreen(
        val editId : Long? = null,
    ) : BasketConfig()
}

sealed class ChildBasket {
    class BasketChild(val component: BasketComponent) : ChildBasket()
    class ListingChild(val component: ListingComponent) : ChildBasket()
    class OfferChild(val component: OfferComponent) : ChildBasket()
    class UserChild(val component: UserComponent) : ChildBasket()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildBasket()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildBasket()
    class MessengerChild(val component: DialogsComponent) : ChildBasket()
    class ProposalChild(val component: ProposalComponent) : ChildBasket()
    class CreateSubscriptionChild(val component: CreateSubscriptionComponent) : ChildBasket()
}

@Composable
fun BasketNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildBasket>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildBasket.BasketChild -> BasketContent(screen.component)
            is ChildBasket.ListingChild -> ListingContent(screen.component, modifier)
            is ChildBasket.OfferChild -> OfferContent(screen.component, modifier)
            is ChildBasket.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildBasket.UserChild -> UserContent(screen.component, modifier)
            is ChildBasket.CreateOrderChild -> CreateOrderContent(screen.component)
            is ChildBasket.MessengerChild -> DialogsContent(screen.component, modifier)
            is ChildBasket.ProposalChild -> ProposalContent(screen.component)
            is ChildBasket.CreateSubscriptionChild -> CreateSubscriptionContent(screen.component)
        }
    }
}


fun createBasketChild(
    config: BasketConfig,
    componentContext: ComponentContext,
    basketNavigation : StackNavigation<BasketConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToConversations: () -> Unit,
    navigateToSubscribe: () -> Unit,
): ChildBasket =
    when (config) {
        BasketConfig.BasketScreen -> ChildBasket.BasketChild(
            DefaultBasketComponent(
                componentContext = componentContext,
                navigateToListing = {
                    basketNavigation.pushNew(ListingScreen(LD(), SD(), getCurrentDate()))
                },
                navigateToUser = {
                    basketNavigation.pushNew(UserScreen(it, getCurrentDate(), false))
                },
                navigateToOffer = {
                    basketNavigation.pushNew(OfferScreen(it, getCurrentDate()))
                },
                navigateToCreateOrder = {
                    basketNavigation.pushNew(BasketConfig.CreateOrderScreen(it))
                }
            )
        )

        is ListingScreen -> {
            val ld = ListingData(
                searchData = config.searchData,
                data = config.listingData
            )
            ChildBasket.ListingChild(
                DefaultListingComponent(
                    componentContext = componentContext,
                    listingData = ld,
                    selectOffer = {
                        basketNavigation.pushNew(OfferScreen(it, getCurrentDate()))
                    },
                    selectedBack = {
                        basketNavigation.pop()
                    },
                    navigateToSubscribe = {
                        navigateToSubscribe()
                    },
                    navigateToListing = { data ->
                        basketNavigation.pushNew(
                            ListingScreen(
                                data.data,
                                data.searchData,
                                getCurrentDate()
                            )
                        )
                    },
                    navigateToNewSubscription = { id->
                        basketNavigation.pushNew(
                            BasketConfig.CreateSubscriptionScreen(id)
                        )
                    },
                    isOpenSearch = false
                )
            )
        }

        is OfferScreen -> ChildBasket.OfferChild(
            component =
                DefaultOfferComponent(
                    config.id,
                    config.isSnap,
                    componentContext,
                    selectOffer = { newId->
                        basketNavigation.pushNew(
                            OfferScreen(newId, getCurrentDate())
                        )
                    },
                    navigationBack = {
                        basketNavigation.pop()
                    },
                    navigationListing = {
                        basketNavigation.pushNew(
                            ListingScreen(it.data, it.searchData, getCurrentDate())
                        )
                    },
                    navigateToUser = { ui, about ->
                        basketNavigation.pushNew(
                            UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    navigationCreateOffer = { type, catPath, offerId, externalImages ->
                        basketNavigation.pushNew(
                            BasketConfig.CreateOfferScreen(
                                catPath = catPath,
                                createOfferType = type,
                                externalImages = externalImages,
                                offerId = offerId
                            )
                        )
                    },
                    navigateToCreateOrder = { item ->
                        basketNavigation.pushNew(
                            BasketConfig.CreateOrderScreen(item)
                        )
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDialog = { dialogId ->
                        if(dialogId != null)
                            basketNavigation.pushNew(BasketConfig.MessengerScreen(dialogId, getCurrentDate()))
                        else
                            navigateToConversations()
                    },
                    navigationSubscribes = {
                        navigateToSubscribe()
                    },
                    navigateToProposalPage = { offerId, type ->
                        basketNavigation.pushNew(
                            BasketConfig.ProposalScreen(offerId, type, getCurrentDate())
                        )
                    },
                    navigateDynamicSettings = { type, owner ->
                        goToDynamicSettings(type, owner, null)
                    }
                )
        )

        is BasketConfig.CreateOfferScreen -> ChildBasket.CreateOfferChild(
            component =
                DefaultCreateOfferComponent(
                    catPath = config.catPath,
                    offerId = config.offerId,
                    type = config.createOfferType,
                    externalImages = config.externalImages,
                    componentContext,
                    navigateToOffer = { id->
                        basketNavigation.pushNew(
                            OfferScreen(id, getCurrentDate())
                        )
                    },
                    navigateToCreateOffer = { id, path, t ->
                        basketNavigation.replaceCurrent(
                            BasketConfig.CreateOfferScreen(
                                catPath = path,
                                offerId = id,
                                createOfferType = t,
                            )
                        )
                    },
                    navigateBack = {
                        basketNavigation.pop()
                    }
                )
        )
        is UserScreen -> ChildBasket.UserChild(
            DefaultUserComponent(
                userId = config.userId,
                isClickedAboutMe = config.aboutMe,
                componentContext = componentContext,
                goToListing = {
                    basketNavigation.pushNew(
                        ListingScreen(it.data, it.searchData, getCurrentDate())
                    )
                },
                navigateBack = {
                    basketNavigation.pop()
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                },
                navigateToSnapshot = { id ->
                    basketNavigation.pushNew(
                        OfferScreen(id, getCurrentDate(), true)
                    )
                },
                navigateToUser = {
                    basketNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToSubscriptions = {
                    navigateToSubscribe()
                },
            )
        )

        is BasketConfig.CreateOrderScreen -> ChildBasket.CreateOrderChild(
            createOrderFactory(
                componentContext,
                config.basketItem,
                navigateBack = {
                    basketNavigation.pop()
                },
                navigateOffer = { id ->
                    basketNavigation.pushNew(
                        OfferScreen(id, getCurrentDate())
                    )
                },
                navigateUser = { id ->
                    basketNavigation.pushNew(
                        UserScreen(id, getCurrentDate(), false)
                    )
                },
                navigateToMyOrders = {
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

        is BasketConfig.MessengerScreen -> ChildBasket.MessengerChild(
            component =
                DefaultDialogsComponent(
                    componentContext = componentContext,
                    dialogId = config.id,
                    message = null,
                    navigateBack = {
                        basketNavigation.pop()
                    },
                    navigateToOrder = { id, type ->
                        navigateToMyOrders(id, type)
                    },
                    navigateToUser = {
                        basketNavigation.pushNew(
                            UserScreen(it, getCurrentDate(), false)
                        )
                    },
                    navigateToOffer = {
                        basketNavigation.pushNew(
                            OfferScreen(it, getCurrentDate())
                        )
                    },
                    navigateToListingSelected = {
                        basketNavigation.pushNew(
                            ListingScreen(it.data, it.searchData, getCurrentDate())
                        )
                    }
                )
        )
        is BasketConfig.ProposalScreen -> ChildBasket.ProposalChild(
            component = proposalFactory(
                config.offerId,
                config.proposalType,
                componentContext,
                navigateBack = {
                    basketNavigation.pop()
                },
                navigateToOffer = {
                    basketNavigation.pop()
                },
                navigateToUser = {
                    basketNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                }
            )
        )

        is BasketConfig.CreateSubscriptionScreen -> ChildBasket.CreateSubscriptionChild(
            component =
                DefaultCreateSubscriptionComponent(
                    componentContext,
                    config.editId,
                    navigateBack = {
                        basketNavigation.pop()
                    },
                )
        )
    }
