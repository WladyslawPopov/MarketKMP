package market.engine.fragments.root.main.listing

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
import market.engine.core.data.baseFilters.ListingData
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
import market.engine.fragments.root.main.listing.SearchConfig.CreateOfferScreen
import market.engine.fragments.root.main.listing.SearchConfig.ListingScreen
import market.engine.fragments.root.main.listing.SearchConfig.OfferScreen
import market.engine.fragments.root.main.listing.SearchConfig.UserScreen
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
        animation = backAnimation(
            backHandler = when (val screen = stack.active.instance) {
                is ChildSearch.ListingChild -> screen.component.model.value.backHandler
                is ChildSearch.OfferChild -> screen.component.model.value.backHandler
                is ChildSearch.UserChild -> screen.component.model.value.backHandler
                is ChildSearch.CreateOfferChild -> screen.component.model.value.backHandler
                is ChildSearch.CreateOrderChild -> screen.component.model.value.backHandler
                is ChildSearch.MessageChild -> screen.component.model.value.backHandler
                is ChildSearch.ProposalChild -> screen.component.model.value.backHandler
                is ChildSearch.CreateSubscriptionChild -> screen.component.model.value.backHandler
            },
            onBack = {
                when (val screen = stack.active.instance) {
                    is ChildSearch.ListingChild -> screen.component.goBack()
                    is ChildSearch.OfferChild -> screen.component.onBackClick()
                    is ChildSearch.UserChild -> screen.component.onBack()
                    is ChildSearch.CreateOfferChild -> screen.component.onBackClicked()
                    is ChildSearch.CreateOrderChild -> screen.component.onBackClicked()
                    is ChildSearch.MessageChild -> screen.component.onBackClicked()
                    is ChildSearch.ProposalChild -> screen.component.goBack()
                    is ChildSearch.CreateSubscriptionChild -> screen.component.onBackClicked()
                }
            }
        ),
    ) { child ->
        when (val screen = child.instance) {
            is ChildSearch.ListingChild -> ListingContent(screen.component, modifier)
            is ChildSearch.OfferChild -> OfferContent(screen.component)
            is ChildSearch.UserChild -> UserContent(screen.component, modifier)
            is ChildSearch.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildSearch.CreateOrderChild -> CreateOrderContent(screen.component)
            is ChildSearch.MessageChild -> DialogsContent(screen.component, modifier)
            is ChildSearch.ProposalChild -> ProposalContent(screen.component)
            is ChildSearch.CreateSubscriptionChild -> CreateSubscriptionContent(screen.component)
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
fun createSearchChild(
    config: SearchConfig,
    componentContext: JetpackComponentContext,
    searchNavigation: StackNavigation<SearchConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToSubscribe: () -> Unit,
    navigateToConversations: () -> Unit,
): ChildSearch =
    when (config) {
        is ListingScreen -> {
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
                            OfferScreen(
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
                            ListingScreen(
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
        is OfferScreen -> ChildSearch.OfferChild(
            component =
                DefaultOfferComponent(
                    config.id,
                    config.isSnapshot,
                    componentContext,
                    selectOffer = { newId->
                        searchNavigation.pushNew(
                            OfferScreen(newId, getCurrentDate())
                        )
                    },
                    navigationBack = {
                        searchNavigation.pop()
                    },
                    navigationListing = {
                        searchNavigation.pushNew(
                            ListingScreen(it.data, it.searchData, false, getCurrentDate())
                        )
                    },
                    navigateToUser = { ui, about ->
                        searchNavigation.pushNew(
                            UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    navigationCreateOffer = { type, catPath, offerId, externalImages ->
                        searchNavigation.pushNew(
                            CreateOfferScreen(
                                catPath = catPath,
                                createOfferType = type,
                                externalImages = externalImages,
                                offerId = offerId
                            )
                        )
                    },
                    navigateToCreateOrder = { item ->
                        searchNavigation.pushNew(
                            SearchConfig.CreateOrderScreen(item)
                        )
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDialog = { dialogId ->
                        if(dialogId != null)
                            searchNavigation.pushNew(SearchConfig.MessageScreen(dialogId, getCurrentDate()))
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
                    },
                    navigateDynamicSettings = { type, owner ->
                        goToDynamicSettings(type, owner, null)
                    }
                )
        )
        is UserScreen -> ChildSearch.UserChild(
            component =
                DefaultUserComponent(
                    userId = config.id,
                    isClickedAboutMe = config.aboutMe,
                    componentContext = componentContext,
                    goToListing = {
                        searchNavigation.pushNew(
                            ListingScreen(it.data, it.searchData,false, getCurrentDate())
                        )
                    },
                    navigateBack = {
                        searchNavigation.pop()
                    },
                    navigateToOrder = { id, type ->
                        navigateToMyOrders(id, type)
                    },
                    navigateToSnapshot = { id ->
                        searchNavigation.pushNew(
                            OfferScreen(id, getCurrentDate(), true)
                        )
                    },
                    navigateToUser = {
                        searchNavigation.pushNew(
                            UserScreen(it, getCurrentDate(), false)
                        )
                    },
                    navigateToSubscriptions = {
                        navigateToSubscribe()
                    },
                )
        )
        is CreateOfferScreen -> ChildSearch.CreateOfferChild(
            component =
                DefaultCreateOfferComponent(
                    catPath = config.catPath,
                    offerId = config.offerId,
                    type = config.createOfferType,
                    externalImages = config.externalImages,
                    componentContext,
                    navigateToOffer = { id->
                        searchNavigation.pushNew(
                            OfferScreen(id, getCurrentDate())
                        )
                    },
                    navigateToCreateOffer = { id, path, t ->
                        searchNavigation.replaceCurrent(
                            CreateOfferScreen(
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
            component = DefaultCreateOrderComponent(
                componentContext,
                config.basketItem,
                navigateToOffer = { id->
                    searchNavigation.pushNew(
                        OfferScreen(id, getCurrentDate())
                    )
                },
                navigateBack = {
                    searchNavigation.pop()
                },
                navigateToUser = { id->
                    searchNavigation.pushNew(
                        UserScreen(id, getCurrentDate(), false)
                    )
                },
                navigateToMyOrders = {
                    searchNavigation.pop()
                    navigateToMyOrders(null, DealTypeGroup.BUY)
                }
            )
        )

        is SearchConfig.MessageScreen -> ChildSearch.MessageChild(
            component = DefaultDialogsComponent(
                componentContext = componentContext,
                dialogId = config.id,
                message = null,
                navigateBack = {
                    searchNavigation.pop()
                },
                navigateToOrder = { id, type ->
                    navigateToMyOrders(id, type)
                },
                navigateToUser = {
                    searchNavigation.pushNew(
                        UserScreen(it, getCurrentDate(), false)
                    )
                },
                navigateToOffer = {
                    searchNavigation.pushNew(
                        OfferScreen(it, getCurrentDate())
                    )
                },
                navigateToListingSelected = {
                    searchNavigation.pushNew(
                        ListingScreen(it.data, it.searchData, false, getCurrentDate())
                    )
                }
            )
        )

        is SearchConfig.ProposalScreen -> ChildSearch.ProposalChild(
            component =
                DefaultProposalComponent(
                    offerId = config.offerId,
                    proposalType = config.proposalType,
                    componentContext = componentContext,
                    navigateToOffer = {
                        searchNavigation.pushNew(
                            OfferScreen(it, getCurrentDate())
                        )
                    },
                    navigateToUser = {
                        searchNavigation.pushNew(
                            UserScreen(it, getCurrentDate(), false)
                        )
                    },
                    navigateBack = {
                        searchNavigation.pop()
                    }
                )
        )

        is SearchConfig.CreateSubscriptionScreen -> {
            ChildSearch.CreateSubscriptionChild(
                component =
                    DefaultCreateSubscriptionComponent(
                        componentContext,
                        config.editId,
                        navigateBack = {
                            searchNavigation.pop()
                        },
                    )
            )
        }
    }
