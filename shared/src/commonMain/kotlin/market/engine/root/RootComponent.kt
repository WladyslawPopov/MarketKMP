package market.engine.root

import market.engine.ui.home.DefaultHomeComponent
import application.market.auction_mobile.ui.search.DefaultSearchComponent
import market.engine.ui.home.HomeComponent
import application.market.auction_mobile.ui.search.SearchComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.ui.basket.BasketComponent
import market.engine.ui.basket.DefaultBasketComponent
import market.engine.ui.favorites.DefaultFavoritesComponent
import market.engine.ui.favorites.FavoritesComponent
import market.engine.ui.profile.DefaultProfileComponent
import market.engine.ui.profile.ProfileComponent

interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class HomeChild(val component: HomeComponent) : Child()
        class SearchChild(val component: SearchComponent) : Child()
        class BasketChild(val component: BasketComponent) : Child()
        class FavoritesChild(val component: FavoritesComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
    }

    fun navigateTo(config: Config)
}

class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(), // Or null to disable navigation state saving
            initialConfiguration = Config.Home,
            handleBackButton = true, // Pop the back stack on back button press
            childFactory = ::createChild,
        )

    override fun navigateTo(config: Config) {
        navigation.replaceCurrent(config)
    }

    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child =
        when (config) {
            is Config.Home -> RootComponent.Child.HomeChild(itemHome(componentContext))
            is Config.Search -> RootComponent.Child.SearchChild(
                itemSearch(
                    componentContext,
                    config
                )
            )
            is Config.Basket -> RootComponent.Child.BasketChild(itemBasket(config, componentContext))
            is Config.Favorites -> RootComponent.Child.FavoritesChild(itemFavorites(config, componentContext))
            is Config.Profile -> RootComponent.Child.ProfileChild(itemProfile(config, componentContext))
        }


    private fun itemHome(componentContext: ComponentContext): HomeComponent {
        return DefaultHomeComponent(
            componentContext = componentContext,
            onItemSelected = { navigation.push(Config.Home) }
        )
    }

    private fun itemSearch(
        componentContext: ComponentContext,
        config: Config.Search
    ): SearchComponent =
        DefaultSearchComponent(
            componentContext = componentContext,
            itemId = config.itemId,
            onBackPressed = { navigation.pop() }
        )

    private fun itemBasket(
        config: Config.Basket,
        componentContext: ComponentContext
    ): BasketComponent =
        DefaultBasketComponent(
            componentContext = componentContext,
            onBackPressed = { navigation.pop() }
        )

    private fun itemFavorites(
        config: Config.Favorites,
        componentContext: ComponentContext
    ): FavoritesComponent =
        DefaultFavoritesComponent(
            componentContext = componentContext,
            onBackPressed = { navigation.pop() }
        )
    private fun itemProfile(
        config: Config.Profile,
        componentContext: ComponentContext
    ): ProfileComponent =
        DefaultProfileComponent(
            componentContext = componentContext,
            onBackPressed = { navigation.pop() }
        )
}

@Serializable // kotlinx-serialization plugin must be applied
sealed class Config {
    @Serializable
    data object Home : Config()

    @Serializable
    data class Search(val itemId: Long) : Config()

    @Serializable
    data object Basket : Config()

    @Serializable
    data object Favorites : Config()

    @Serializable
    data object Profile : Config()
}

