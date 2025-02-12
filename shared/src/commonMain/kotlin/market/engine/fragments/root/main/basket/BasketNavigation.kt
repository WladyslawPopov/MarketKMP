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
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.getCurrentDate
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
import market.engine.fragments.root.main.proposalPage.ProposalComponent
import market.engine.fragments.root.main.proposalPage.ProposalContent
import market.engine.fragments.root.main.proposalPage.proposalFactory
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent
import market.engine.fragments.root.main.user.userFactory

@Serializable
sealed class BasketConfig {
    @Serializable
    data object BasketScreen : BasketConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : BasketConfig()

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
    data class MessengerScreen(val id: Long) : BasketConfig()

    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType) : BasketConfig()
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
        }
    }
}


fun createBasketChild(
    config: BasketConfig,
    componentContext: ComponentContext,
    basketNavigation : StackNavigation<BasketConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToConversations: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToSubscribe: () -> Unit,
): ChildBasket =
    when (config) {
        BasketConfig.BasketScreen -> ChildBasket.BasketChild(
            itemBasket(componentContext, basketNavigation)
        )

        is BasketConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = MutableValue(config.searchData),
                data = MutableValue(config.listingData)
            )
            ChildBasket.ListingChild(
                listingFactory(
                    componentContext,
                    ld,
                    selectOffer = {
                        basketNavigation.pushNew(BasketConfig.OfferScreen(it, getCurrentDate()))
                    },
                    onBack = {
                        basketNavigation.pop()
                    },
                    isOpenCategory = false,
                    navigateToSubscribe = {
                        navigateToSubscribe()
                    }
                )
            )
        }

        is BasketConfig.OfferScreen -> ChildBasket.OfferChild(
            component = offerFactory(
                componentContext,
                config.id,
                selectOffer = {
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                onBack = {
                    basketNavigation.pop()
                },
                onListingSelected = {
                    basketNavigation.pushNew(
                        BasketConfig.ListingScreen(it.data.value, it.searchData.value)
                    )
                },
                onUserSelected = { ui, about ->
                    basketNavigation.pushNew(
                        BasketConfig.UserScreen(ui, getCurrentDate(), about)
                    )
                },
                isSnapshot = config.isSnap,
                navigateToCreateOffer = { type, catPath, offerId, externalImages ->
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
                    navigateToLogin()
                },
                navigateToDialog = { dialogId ->
                    if(dialogId != null)
                        basketNavigation.pushNew(BasketConfig.MessengerScreen(dialogId))
                    else
                        navigateToConversations()
                },
                navigationSubscribes = {
                    navigateToSubscribe()
                },
                navigateToProposalPage = { offerId, type ->
                    basketNavigation.pushNew(
                        BasketConfig.ProposalScreen(offerId, type)
                    )
                }
            )
        )

        is BasketConfig.CreateOfferScreen -> ChildBasket.CreateOfferChild(
            component = createOfferFactory(
                componentContext = componentContext,
                catPath = config.catPath,
                offerId = config.offerId,
                type = config.createOfferType,
                externalImages = config.externalImages,
                navigateOffer = { id ->
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(id, getCurrentDate())
                    )
                },
                navigateCreateOffer = { id, path, t ->
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
        is BasketConfig.UserScreen -> ChildBasket.UserChild(
            userFactory(
                componentContext,
                config.userId,
                config.aboutMe,
                goToLogin = {
                    basketNavigation.pushNew(
                        BasketConfig.ListingScreen(it.data.value, it.searchData.value)
                    )
                },
                goBack = {
                    basketNavigation.pop()
                },
                goToSnapshot = { id ->
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(id, getCurrentDate(), true)
                    )
                },
                goToUser = {
                    basketNavigation.pushNew(
                        BasketConfig.UserScreen(it, getCurrentDate(), false)
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

        is BasketConfig.CreateOrderScreen -> ChildBasket.CreateOrderChild(
            createOrderFactory(
                componentContext,
                config.basketItem,
                navigateBack = {
                    basketNavigation.pop()
                },
                navigateOffer = { id ->
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(id, getCurrentDate())
                    )
                },
                navigateUser = { id ->
                    basketNavigation.pushNew(
                        BasketConfig.UserScreen(id, getCurrentDate(), false)
                    )
                },
                navigateToMyOrders = {
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

        is BasketConfig.MessengerScreen -> ChildBasket.MessengerChild(
            component = messengerFactory(
                componentContext,
                config.id,
                navigateBack = {
                    basketNavigation.pop()
                },
                navigateToUser = {
                    basketNavigation.pushNew(
                        BasketConfig.UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToOffer = {
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
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
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToUser = {
                    basketNavigation.pushNew(
                        BasketConfig.UserScreen(it, getCurrentDate(), false)
                    )
                }
            )
        )
    }

fun itemBasket(componentContext: ComponentContext, basketNavigation : StackNavigation<BasketConfig>): BasketComponent {
    return DefaultBasketComponent(
        componentContext = componentContext,
        navigateToListing = {
            basketNavigation.pushNew(BasketConfig.ListingScreen(LD(), SD()))
        },
        navigateToUser = {
            basketNavigation.pushNew(BasketConfig.UserScreen(it, getCurrentDate(), false))
        },
        navigateToOffer = {
            basketNavigation.pushNew(BasketConfig.OfferScreen(it, getCurrentDate()))
        },
        navigateToCreateOrder = {
            basketNavigation.pushNew(BasketConfig.CreateOrderScreen(it))
        }
    )
}
