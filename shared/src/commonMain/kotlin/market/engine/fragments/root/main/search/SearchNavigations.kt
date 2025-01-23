package market.engine.fragments.root.main.search

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
import market.engine.fragments.createOffer.createOfferFactory
import market.engine.fragments.offer.offerFactory
import market.engine.fragments.user.userFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.createOrder.CreateOrderComponent
import market.engine.fragments.createOrder.CreateOrderContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent
import market.engine.fragments.createOrder.createOrderFactory
import market.engine.fragments.listing.listingFactory

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
}

sealed class ChildSearch {
    class ListingChild(val component: ListingComponent) : ChildSearch()
    class OfferChild(val component: OfferComponent) : ChildSearch()
    class UserChild(val component: UserComponent) : ChildSearch()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildSearch()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildSearch()
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
            is ChildSearch.ListingChild -> ListingContent(screen.component, modifier,false)
            is ChildSearch.OfferChild -> OfferContent(screen.component, modifier)
            is ChildSearch.UserChild -> UserContent(screen.component, modifier)
            is ChildSearch.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildSearch.CreateOrderChild -> CreateOrderContent(screen.component)
        }
    }
}

fun createSearchChild(
    config: SearchConfig,
    componentContext: ComponentContext,
    searchNavigation: StackNavigation<SearchConfig>,
    navigateToMyOrders: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToDialog: (dialogId: Long?) -> Unit
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
                    isOpenSearch = config.isOpenSearch
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
                    navigateToDialog(dialogId)
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
                    navigateToMyOrders()
                }
            )
        )
    }
