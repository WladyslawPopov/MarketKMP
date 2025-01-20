package market.engine.fragments.root.main.favorites

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
import market.engine.fragments.createOffer.createOfferFactory
import market.engine.fragments.listing.listingFactory
import market.engine.fragments.offer.offerFactory
import market.engine.fragments.user.userFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.FavScreenType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.createOrder.CreateOrderComponent
import market.engine.fragments.createOrder.CreateOrderContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.root.main.favorites.subscriptions.DefaultSubscribesComponent
import market.engine.fragments.root.main.favorites.subscriptions.SubscribesComponent
import market.engine.fragments.root.main.favorites.subscriptions.SubscribesContent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent
import market.engine.fragments.createOrder.createOrderFactory

@Serializable
sealed class FavoritesConfig {
    @Serializable
    data object FavoritesScreen : FavoritesConfig()

    @Serializable
    data object SubscriptionsScreen : FavoritesConfig()

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
}


sealed class ChildFavorites {
    class FavoritesChild(val component: FavoritesComponent) : ChildFavorites()
    class SubChild(val component: SubscribesComponent) : ChildFavorites()
    class OfferChild(val component: OfferComponent) : ChildFavorites()
    class ListingChild(val component: ListingComponent) : ChildFavorites()
    class UserChild(val component: UserComponent) : ChildFavorites()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildFavorites()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildFavorites()
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
            is ChildFavorites.FavoritesChild -> FavoritesContent(modifier, screen.component)
            is ChildFavorites.SubChild -> SubscribesContent(modifier, screen.component)
            is ChildFavorites.OfferChild -> OfferContent(screen.component, modifier)
            is ChildFavorites.ListingChild -> ListingContent(screen.component, modifier)
            is ChildFavorites.UserChild -> UserContent(screen.component, modifier)
            is ChildFavorites.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildFavorites.CreateOrderChild -> CreateOrderContent(screen.component)
        }
    }
}


fun createFavoritesChild(
    config: FavoritesConfig,
    componentContext: ComponentContext,
    favoritesNavigation : StackNavigation<FavoritesConfig>,
    navigateToMyOrders: () -> Unit
): ChildFavorites =
    when (config) {
        FavoritesConfig.FavoritesScreen -> ChildFavorites.FavoritesChild(
            itemFavorites(componentContext, favoritesNavigation)
        )

        FavoritesConfig.SubscriptionsScreen -> ChildFavorites.SubChild(
            itemSubscriptions(componentContext, favoritesNavigation)
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
                    isOpenCategory = false
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
                    navigateToMyOrders()
                }
            )
        )
    }

fun itemSubscriptions(componentContext: ComponentContext, favoritesNavigation : StackNavigation<FavoritesConfig>): SubscribesComponent {
    return DefaultSubscribesComponent(
        componentContext = componentContext,
    ) {
        pushFavStack(FavScreenType.FAVORITES, favoritesNavigation = favoritesNavigation)
    }
}

fun itemFavorites(componentContext: ComponentContext, favoritesNavigation : StackNavigation<FavoritesConfig>): FavoritesComponent {
    return DefaultFavoritesComponent(
        componentContext = componentContext,
        goToOffer = { id ->
            pushFavStack(FavScreenType.OFFER, id, favoritesNavigation)
        },
        selectedSubscribes = {
            pushFavStack(FavScreenType.SUBSCRIBED, favoritesNavigation = favoritesNavigation)
        }
    )
}

fun pushFavStack(screenType: FavScreenType, id: Long = 1L, favoritesNavigation : StackNavigation<FavoritesConfig>){
    when(screenType){
        FavScreenType.FAVORITES -> {
            favoritesNavigation.replaceAll(FavoritesConfig.FavoritesScreen)
        }
        FavScreenType.SUBSCRIBED -> {
            favoritesNavigation.replaceAll(FavoritesConfig.SubscriptionsScreen)
        }
        FavScreenType.OFFER -> {
            favoritesNavigation.pushNew(FavoritesConfig.OfferScreen(id, getCurrentDate()))
        }
    }
}
