package market.engine.navigation.main.children

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
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.ListingData
import market.engine.navigation.main.publicItems.itemCreateOffer
import market.engine.navigation.main.publicItems.itemListing
import market.engine.navigation.main.publicItems.itemOffer
import market.engine.navigation.main.publicItems.itemUser
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.FavScreenType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.favorites.DefaultFavoritesComponent
import market.engine.fragments.favorites.FavoritesComponent
import market.engine.fragments.favorites.FavoritesContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.subscriptions.DefaultSubscribesComponent
import market.engine.fragments.subscriptions.SubscribesComponent
import market.engine.fragments.subscriptions.SubscribesContent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent

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
        val type : CreateOfferType,
        val externalImages : List<String>? = null
    ) : FavoritesConfig()
}


sealed class ChildFavorites {
    class FavoritesChild(val component: FavoritesComponent) : ChildFavorites()
    class SubChild(val component: SubscribesComponent) : ChildFavorites()
    class OfferChild(val component: OfferComponent) : ChildFavorites()
    class ListingChild(val component: ListingComponent) : ChildFavorites()
    class UserChild(val component: UserComponent) : ChildFavorites()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildFavorites()
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
        }
    }
}


fun createFavoritesChild(
    config: FavoritesConfig,
    componentContext: ComponentContext,
    favoritesNavigation : StackNavigation<FavoritesConfig>
): ChildFavorites =
    when (config) {
        FavoritesConfig.FavoritesScreen -> ChildFavorites.FavoritesChild(
            itemFavorites(componentContext, favoritesNavigation)
        )

        FavoritesConfig.SubscriptionsScreen -> ChildFavorites.SubChild(
            itemSubscriptions(componentContext, favoritesNavigation)
        )

        is FavoritesConfig.OfferScreen -> ChildFavorites.OfferChild(
            component = itemOffer(
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
                            type = type,
                            externalImages = externalImages,
                            offerId = offerId
                        )
                    )
                }
            )
        )

        is FavoritesConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = MutableValue(config.searchData),
                data = MutableValue(config.listingData)
            )
            ChildFavorites.ListingChild(
                component = itemListing(
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
            itemUser(
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
            component = itemCreateOffer(
                componentContext = componentContext,
                catPath = config.catPath,
                offerId = config.offerId,
                type = config.type,
                externalImages = config.externalImages,
                navigateBack = {
                    favoritesNavigation.pop()
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
