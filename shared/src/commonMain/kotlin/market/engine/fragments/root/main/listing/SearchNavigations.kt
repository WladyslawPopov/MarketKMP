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
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.fragments.root.main.user.userFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.createOffer.CreateOfferComponent
import market.engine.fragments.root.main.createOffer.CreateOfferContent
import market.engine.fragments.root.main.createOffer.createOfferFactory
import market.engine.fragments.root.main.createOrder.CreateOrderComponent
import market.engine.fragments.root.main.createOrder.CreateOrderContent
import market.engine.fragments.root.main.createOrder.createOrderFactory
import market.engine.fragments.root.main.messenger.DialogsComponent
import market.engine.fragments.root.main.messenger.DialogsContent
import market.engine.fragments.root.main.messenger.messengerFactory
import market.engine.fragments.root.main.offer.OfferComponent
import market.engine.fragments.root.main.offer.OfferContent
import market.engine.fragments.root.main.offer.offerFactory
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent

@Serializable
sealed class SearchConfig {
    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD, val isOpenSearch : Boolean) : SearchConfig()

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
    data class MessageScreen(val id: Long) : SearchConfig()
}

sealed class ChildSearch {
    class ListingChild(val component: ListingComponent) : ChildSearch()
    class OfferChild(val component: OfferComponent) : ChildSearch()
    class UserChild(val component: UserComponent) : ChildSearch()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildSearch()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildSearch()
    class MessageChild(val component: DialogsComponent) : ChildSearch()
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
        }
    }
}

fun createSearchChild(
    config: SearchConfig,
    componentContext: ComponentContext,
    searchNavigation: StackNavigation<SearchConfig>,
    navigateToMyOrders: (Long?, DealTypeGroup) -> Unit,
    navigateToLogin: () -> Unit,
    navigateToSubscribe: () -> Unit
): ChildSearch =
    when (config) {
        is SearchConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = MutableValue(config.searchData),
                data = MutableValue(config.listingData)
            )

            ChildSearch.ListingChild(
                component = listingFactory(
                    componentContext,
                    ld,
                    selectOffer = {
                        searchNavigation.pushNew(
                            SearchConfig.OfferScreen(
                                it,
                                getCurrentDate()
                            )
                        )
                    },
                    onBack = {
                        searchNavigation.pop()
                    },
                    isOpenCategory = false,
                    isOpenSearch = config.isOpenSearch,
                    navigateToSubscribe = {
                        navigateToSubscribe()
                    }
                ),
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
                        SearchConfig.ListingScreen(it.data.value, it.searchData.value, false)
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
                    navigateToLogin()
                },
                navigateToDialog = { dialogId ->
                    searchNavigation.pushNew(
                        SearchConfig.MessageScreen(dialogId ?: 1L)
                    )
                },
                navigationSubscribes = {
                    navigateToSubscribe()
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
                        SearchConfig.ListingScreen(it.data.value, it.searchData.value, false)
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
                }
            )
        )
    }
