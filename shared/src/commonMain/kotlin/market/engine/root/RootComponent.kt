package market.engine.root

import market.engine.ui.home.DefaultHomeComponent
import market.engine.ui.home.HomeComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.ui.basket.BasketComponent
import market.engine.ui.basket.DefaultBasketComponent
import market.engine.ui.category.CategoryComponent
import market.engine.ui.category.DefaultCategoryComponent
import market.engine.ui.favorites.DefaultFavoritesComponent
import market.engine.ui.favorites.FavoritesComponent
import market.engine.ui.listing.DefaultListingComponent
import market.engine.ui.listing.ListingComponent
import market.engine.ui.profile.DefaultProfileComponent
import market.engine.ui.profile.ProfileComponent
import market.engine.ui.search.DefaultSearchComponent
import market.engine.ui.search.SearchComponent

interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class HomeChild(val component: HomeComponent) : Child()
        class CategoryChild(val component: CategoryComponent) : Child()
        class BasketChild(val component: BasketComponent) : Child()
        class FavoritesChild(val component: FavoritesComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
        class ListingChild(val component: ListingComponent) : Child()
        class SearchChild(val component: SearchComponent) : Child()
    }

    fun navigateToBottomItem(config: Config)

    fun navigateTo(config: Config)

    fun backPressed()
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

    override fun navigateToBottomItem(config: Config) {
        navigation.replaceCurrent(config)
    }

    override fun navigateTo(config: Config) {
        navigation.push(config)
    }

    override fun backPressed() {
        if (childStack.items.size > 1) {
            val child = childStack.items[childStack.items.lastIndex - 1].instance
            when (child) {
                is RootComponent.Child.HomeChild -> navigateToBottomItem(Config.Home)
                is RootComponent.Child.CategoryChild -> navigateToBottomItem(Config.Category)
                is RootComponent.Child.BasketChild -> navigateToBottomItem(Config.Basket)
                is RootComponent.Child.FavoritesChild -> navigateToBottomItem(Config.Favorites)
                is RootComponent.Child.ProfileChild -> navigateToBottomItem(Config.Profile)
                is RootComponent.Child.ListingChild -> navigation.pop()
                is RootComponent.Child.SearchChild -> navigation.pop()
            }
        }else{
            navigation.replaceCurrent(Config.Home)
        }
    }

    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child =
        when (config) {
            is Config.Home -> RootComponent.Child.HomeChild(itemHome(componentContext))
            is Config.Category -> RootComponent.Child.CategoryChild(
                itemCategory(
                    componentContext,
                    config
                )
            )
            is Config.Basket -> RootComponent.Child.BasketChild(itemBasket(config, componentContext))
            is Config.Favorites -> RootComponent.Child.FavoritesChild(itemFavorites(config, componentContext))
            is Config.Profile -> RootComponent.Child.ProfileChild(itemProfile(config, componentContext))
            is Config.Listing -> RootComponent.Child.ListingChild(itemListing(componentContext))
            is Config.Search -> RootComponent.Child.SearchChild(itemSearch(componentContext))
        }

    private fun itemHome(componentContext: ComponentContext): HomeComponent {
        return DefaultHomeComponent(
            componentContext = componentContext,
            onSearchSelected = { navigateToBottomItem(Config.Category) },
            goToListingSelected = { navigateTo(Config.Listing) }
        )
    }

    private fun itemListing(componentContext: ComponentContext): ListingComponent {
        return DefaultListingComponent(
            componentContext = componentContext,
            onBackPressed = { navigation.pop() }
        )
    }

    private fun itemSearch(componentContext: ComponentContext): SearchComponent {
        return DefaultSearchComponent(
            componentContext = componentContext,
            onBackPressed = { navigation.pop() },
            goToListingSelected = { navigateTo(Config.Listing) }
        )
    }

    private fun itemCategory(
        componentContext: ComponentContext,
        config: Config.Category
    ): CategoryComponent =
        DefaultCategoryComponent(
            componentContext = componentContext,
            onBackPressed = { backPressed() },
            goToListingSelected = { navigateTo(Config.Listing) },
            goToSearchSelected = { navigateTo(Config.Search) }
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
    data object Category : Config()

    @Serializable
    data object Basket : Config()

    @Serializable
    data object Favorites : Config()

    @Serializable
    data object Profile : Config()

    @Serializable
    data object Listing : Config()

    @Serializable
    data object Search : Config()
}

