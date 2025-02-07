package market.engine.fragments.root.main.favPages

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
import com.arkivanov.decompose.router.stack.replaceAll
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
import market.engine.core.data.types.FavScreenType
import market.engine.core.utils.getCurrentDate
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
import market.engine.fragments.root.main.offer.OfferComponent
import market.engine.fragments.root.main.offer.OfferContent
import market.engine.fragments.root.main.offer.offerFactory
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent

@Serializable
sealed class FavoritesConfig {
    @Serializable
    data class FavPagesScreen(val favScreenType: FavScreenType) : FavoritesConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnap: Boolean = false) : FavoritesConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : FavoritesConfig()

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
}

sealed class ChildFavorites {
    class FavPagesChild(val component: FavPagesComponent) : ChildFavorites()
    class OfferChild(val component: OfferComponent) : ChildFavorites()
    class ListingChild(val component: ListingComponent) : ChildFavorites()
    class UserChild(val component: UserComponent) : ChildFavorites()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildFavorites()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildFavorites()
    class CreateSubscriptionChild(val component: CreateSubscriptionComponent) : ChildFavorites()
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
        }
    }
}

fun createFavoritesChild(
    config: FavoritesConfig,
    componentContext: ComponentContext,
    favoritesNavigation : StackNavigation<FavoritesConfig>,
    navigateToMyOrders: (Long?) -> Unit,
    navigateToLogin: () -> Unit,
    navigateToDialog: (dialogId: Long?) -> Unit
): ChildFavorites = when (config) {
        is FavoritesConfig.FavPagesScreen -> ChildFavorites.FavPagesChild(
            itemFavPages(componentContext, favoritesNavigation, config.favScreenType)
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
                        FavoritesConfig.ListingScreen(it.data.value, it.searchData.value)
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
                    navigateToLogin()
                },
                navigateToDialog = { dialogId ->
                    navigateToDialog(dialogId)
                },
                navigationSubscribes = {
                    favoritesNavigation.replaceAll(FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED))
                }
            )
        )

        is FavoritesConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = MutableValue(config.searchData),
                data = MutableValue(config.listingData)
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
                        FavoritesConfig.ListingScreen(it.data.value, it.searchData.value)
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
                goToOrder = {
                    navigateToMyOrders(it)
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
                    navigateToMyOrders(null)
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
}

fun itemFavPages(
    componentContext: ComponentContext,
    favoritesNavigation : StackNavigation<FavoritesConfig>,
    favType: FavScreenType = FavScreenType.FAVORITES
): FavPagesComponent {
    return DefaultFavPagesComponent(
        favoritesNavigation = favoritesNavigation,
        componentContext = componentContext,
        favType = favType
    )
}
