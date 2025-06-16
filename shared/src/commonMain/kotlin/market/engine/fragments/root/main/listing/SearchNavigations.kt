package market.engine.fragments.root.main.listing

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
import market.engine.fragments.root.main.createSubscription.CreateSubscriptionComponent
import market.engine.fragments.root.main.createSubscription.CreateSubscriptionContent
import market.engine.fragments.root.main.createSubscription.createSubscriptionFactory
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
sealed class SearchConfig {
    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD, val isOpenSearch : Boolean, val ts : String?) : SearchConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : SearchConfig()

    @Serializable
    data class UserScreen(val id: Long, val ts: String, val aboutMe : Boolean) : SearchConfig()

    @Serializable
    data class CreateOfferScreen(
        val catPath: List<Long>?,
        val offerId: Long? = null,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>? = null
    ) : SearchConfig()

    @Serializable
    data class CreateOrderScreen(
        val basketItem : Pair<Long, List<SelectedBasketItem>>,
    ) : SearchConfig()

    @Serializable
    data class MessageScreen(val id: Long, val ts: String) : SearchConfig()

    @Serializable
    data class ProposalScreen(val offerId: Long, val proposalType: ProposalType, val ts: String?) : SearchConfig()

    @Serializable
    data class CreateSubscriptionScreen(
        val editId : Long? = null,
    ) : SearchConfig()
}

sealed class ChildSearch {
    class ListingChild(val component: ListingComponent) : ChildSearch()
    class OfferChild(val component: OfferComponent) : ChildSearch()
    class UserChild(val component: UserComponent) : ChildSearch()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildSearch()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildSearch()
    class MessageChild(val component: DialogsComponent) : ChildSearch()
    class ProposalChild(val component: ProposalComponent) : ChildSearch()
    class CreateSubscriptionChild(val component: CreateSubscriptionComponent) : ChildSearch()
}

@Composable
fun SearchNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildSearch>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildSearch.ListingChild -> ListingContent(screen.component, modifier)
            is ChildSearch.OfferChild -> OfferContent(screen.component, modifier)
            is ChildSearch.UserChild -> UserContent(screen.component, modifier)
            is ChildSearch.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildSearch.CreateOrderChild -> CreateOrderContent(screen.component)
            is ChildSearch.MessageChild -> DialogsContent(screen.component, modifier)
            is ChildSearch.ProposalChild -> ProposalContent(screen.component)
            is ChildSearch.CreateSubscriptionChild -> CreateSubscriptionContent(screen.component)
        }
    }
}

fun createSearchChild(
    config: SearchConfig,
    componentContext: ComponentContext,
    searchNavigation: StackNavigation<SearchConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToSubscribe: () -> Unit,
    navigateToConversations: () -> Unit,
): ChildSearch =
    when (config) {
        is SearchConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = config.searchData,
                data = config.listingData
            )

            ChildSearch.ListingChild(
                component = DefaultListingComponent(
                    isOpenSearch = config.isOpenSearch,
                    componentContext = componentContext,
                    listingData = ld,
                    selectOffer = { id ->
                        searchNavigation.pushNew(
                            SearchConfig.OfferScreen(
                                id,
                                getCurrentDate()
                            )
                        )
                    },
                    selectedBack = {
                        searchNavigation.pop()
                    },
                    navigateToSubscribe = {
                        navigateToSubscribe()
                    },
                    navigateToListing = { ld ->
                        searchNavigation.pushNew(
                            SearchConfig.ListingScreen(
                                ld.data,
                                ld.searchData,
                                false,
                                getCurrentDate()
                            )
                        )
                    },
                    navigateToNewSubscription = {
                        searchNavigation.pushNew(
                            SearchConfig.CreateSubscriptionScreen(it)
                        )
                    }
                )
            )
        }
        is SearchConfig.OfferScreen -> ChildSearch.OfferChild(
            component = offerFactory(
                componentContext,
                config.id,
                selectOffer = {
                    searchNavigation.pushNew(
                        SearchConfig.OfferScreen(
                            it,
                            getCurrentDate(),
                        )
                    )
                },
                onBack = {
                    searchNavigation.pop()
                },
                onListingSelected = {
                    searchNavigation.pushNew(
                        SearchConfig.ListingScreen(it.data, it.searchData, false, getCurrentDate())
                    )
                },
                onUserSelected = { ui, about ->
                    searchNavigation.pushNew(
                        SearchConfig.UserScreen(ui, getCurrentDate(), about)
                    )
                },
                config.isSnapshot,
                navigateToCreateOffer = { type, catPath, offerId, externalImages ->
                    searchNavigation.pushNew(
                        SearchConfig.CreateOfferScreen(
                            catPath = catPath,
                            createOfferType = type,
                            externalImages = externalImages,
                            offerId = offerId
                        )
                    )
                },
                navigateToCreateOrder = {
                    searchNavigation.pushNew(
                        SearchConfig.CreateOrderScreen(it)
                    )
                },
                navigateToLogin = {
                    goToLogin(false)
                },
                navigateToDialog = { dialogId ->
                    if(dialogId != null)
                        searchNavigation.pushNew(
                            SearchConfig.MessageScreen(dialogId, getCurrentDate())
                        )
                    else
                        navigateToConversations()
                },
                navigationSubscribes = {
                    navigateToSubscribe()
                },
                navigateToProposalPage = { offerId, type ->
                    searchNavigation.pushNew(
                        SearchConfig.ProposalScreen(offerId, type, getCurrentDate())
                    )
                }
            )
        )
        is SearchConfig.UserScreen -> ChildSearch.UserChild(
            component = userFactory(
                componentContext,
                config.id,
                config.aboutMe,
                goToLogin = {
                    searchNavigation.pushNew(
                        SearchConfig.ListingScreen(it.data, it.searchData, false, getCurrentDate())
                    )
                },
                goBack = {
                    searchNavigation.pop()
                },
                goToSnapshot = { id ->
                    searchNavigation.pushNew(
                        SearchConfig.OfferScreen(id, getCurrentDate(), true)
                    )
                },
                goToUser = {
                    searchNavigation.pushNew(
                        SearchConfig.UserScreen(it, getCurrentDate(), false)
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
        is SearchConfig.CreateOfferScreen -> ChildSearch.CreateOfferChild(
            component = createOfferFactory(
                componentContext = componentContext,
                catPath = config.catPath,
                offerId = config.offerId,
                type = config.createOfferType,
                externalImages = config.externalImages,
                navigateOffer = { id ->
                    searchNavigation.pushNew(
                        SearchConfig.OfferScreen(id, getCurrentDate())
                    )
                },
                navigateCreateOffer = { id, path, t ->
                    searchNavigation.replaceCurrent(
                        SearchConfig.CreateOfferScreen(
                            catPath = path,
                            offerId = id,
                            createOfferType = t,
                        )
                    )
                },
                navigateBack = {
                    searchNavigation.pop()
                }
            )
        )

        is SearchConfig.CreateOrderScreen -> ChildSearch.CreateOrderChild(
            component = createOrderFactory(
                componentContext = componentContext,
                selectedItems = config.basketItem,
                navigateUser = {
                    searchNavigation.pushNew(
                        SearchConfig.UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateOffer = {
                    searchNavigation.pushNew(
                        SearchConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateBack = {
                    searchNavigation.pop()
                },
                navigateToMyOrders = {
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

        is SearchConfig.MessageScreen -> ChildSearch.MessageChild(
            component = messengerFactory(
                componentContext = componentContext,
                dialogId = config.id,
                navigateBack = {
                    searchNavigation.pop()
                },
                navigateToUser = {
                    searchNavigation.pushNew(
                        SearchConfig.UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToOffer = {
                    searchNavigation.pushNew(
                        SearchConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                },
                navigateToListingSelected = {
                    searchNavigation.pushNew(
                        SearchConfig.ListingScreen(it.data, it.searchData, false, getCurrentDate())
                    )
                }
            )
        )

        is SearchConfig.ProposalScreen -> ChildSearch.ProposalChild(
            component = proposalFactory(
                componentContext = componentContext,
                offerId = config.offerId,
                proposalType = config.proposalType,
                navigateBack = {
                    searchNavigation.pop()
                },
                navigateToUser = {
                    searchNavigation.pushNew(
                        SearchConfig.UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToOffer = {
                    searchNavigation.pop()
                }
            )
        )

        is SearchConfig.CreateSubscriptionScreen -> {
            ChildSearch.CreateSubscriptionChild(
                component = createSubscriptionFactory(
                    componentContext = componentContext,
                    editId = config.editId,
                    navigateBack = {
                        searchNavigation.pop()
                    }
                )
            )
        }
    }
