package market.engine.presentation.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import market.engine.core.globalData.SD
import market.engine.core.types.CategoryScreenType
import market.engine.presentation.category.CategoryComponent
import market.engine.presentation.category.DefaultCategoryComponent
import market.engine.presentation.favorites.DefaultFavoritesComponent
import market.engine.presentation.favorites.FavoritesComponent
import market.engine.presentation.home.DefaultHomeComponent
import market.engine.presentation.home.HomeComponent
import market.engine.presentation.listing.DefaultListingComponent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.profile.DefaultProfileComponent
import market.engine.presentation.profile.ProfileComponent
import market.engine.presentation.search.DefaultSearchComponent
import market.engine.presentation.search.SearchComponent
import org.koin.mp.KoinPlatform.getKoin

interface MainComponent {

    val modelNavigation: Value<ModelNavigation>

    val childStack: Value<ChildStack<*, Child>>

    val childHomeStack: Value<ChildStack<*, ChildHome>>

    val childCategoryStack: Value<ChildStack<*, ChildCategory>>

    val childBasketStack: Value<ChildStack<*, ChildBasket>>

    val childFavoritesStack: Value<ChildStack<*, ChildFavorites>>

    val childProfileStack: Value<ChildStack<*, ChildProfile>>

    val searchData : StateFlow<SD>

    data class ModelNavigation(
        val homeNavigation : StackNavigation<HomeConfig>,
        val categoryNavigation : StackNavigation<CategoryConfig>,
        val basketNavigation : StackNavigation<BasketConfig>,
        val favoritesNavigation : StackNavigation<FavoritesConfig>,
        val profileNavigation : StackNavigation<ProfileConfig>,
    )

    sealed class Child {
        data object HomeChild : Child()
        data object CategoryChild : Child()
        data object BasketChild : Child()
        data object FavoritesChild : Child()
        data object ProfileChild : Child()
    }

    sealed class ChildHome {
        class HomeChild(val component: HomeComponent) : ChildHome()
    }

    sealed class ChildCategory {
        class CategoryChild(val component: CategoryComponent) : ChildCategory()
        class ListingChild(val component: ListingComponent) : ChildCategory()
        class SearchChild(val component: SearchComponent) : ChildCategory()
    }

    sealed class ChildBasket {
        class BasketChild(val component: market.engine.presentation.basket.BasketComponent) : ChildBasket()
    }

    sealed class ChildFavorites {
        class FavoritesChild(val component: FavoritesComponent) : ChildFavorites()
    }

    sealed class ChildProfile {
        class ProfileChild(val component: ProfileComponent) : ChildProfile()
    }

    fun navigateToBottomItem(config: Config)

    fun backPressedBottomItem()

    fun goToLogin()
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    val goToLoginSelected: () -> Unit
) : MainComponent, ComponentContext by componentContext {

    override val searchData : StateFlow<SD> = getKoin().get()

    private val _modelNavigation = MutableValue(
        MainComponent.ModelNavigation(
            homeNavigation = StackNavigation(),
            categoryNavigation = StackNavigation(),
            basketNavigation = StackNavigation(),
            favoritesNavigation = StackNavigation(),
            profileNavigation = StackNavigation(),
        )
    )

    override val modelNavigation = _modelNavigation

    private var currentNavigation = StackNavigation<Config>()

    override val childHomeStack: Value<ChildStack<*, MainComponent.ChildHome>> by lazy {
        childStack(
            source = modelNavigation.value.homeNavigation,
            initialConfiguration = HomeConfig.HomeScreen,
            serializer = HomeConfig.serializer(),
            handleBackButton = true,
            childFactory = ::createChild,
            key = "HomeStack"
        )
    }

    override val childCategoryStack: Value<ChildStack<*, MainComponent.ChildCategory>> =
        childStack(
            source = modelNavigation.value.categoryNavigation,
            initialConfiguration = CategoryConfig.CategoryScreen,
            serializer = CategoryConfig.serializer(),
            handleBackButton = true,
            childFactory = ::createChild,
            key = "CategoryStack"
        )


    override val childBasketStack: Value<ChildStack<*, MainComponent.ChildBasket>> by lazy {
        childStack(
            source = modelNavigation.value.basketNavigation,
            initialConfiguration = BasketConfig.BasketScreen,
            serializer = BasketConfig.serializer(),
            handleBackButton = true,
            childFactory = ::createChild,
            key = "BasketStack"
        )
    }

    override val childFavoritesStack: Value<ChildStack<*, MainComponent.ChildFavorites>> by lazy {
        childStack(
            source = modelNavigation.value.favoritesNavigation,
            initialConfiguration = FavoritesConfig.FavoritesScreen,
            serializer = FavoritesConfig.serializer(),
            handleBackButton = true,
            childFactory = ::createChild,
            key = "FavoritesStack"
        )
    }

    override val childProfileStack: Value<ChildStack<*, MainComponent.ChildProfile>> by lazy {
        childStack(
            source = modelNavigation.value.profileNavigation,
            initialConfiguration = ProfileConfig.ProfileScreen,
            serializer = ProfileConfig.serializer(),
            handleBackButton = true,
            childFactory = ::createChild,
            key = "ProfileStack"
        )
    }

    override val childStack: Value<ChildStack<*, MainComponent.Child>> =
        childStack(
            source = currentNavigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Home,
            handleBackButton = true,
            childFactory = {config, _ ->
                createChild(config)
            },
            key = "MainStack"
        )

    override fun navigateToBottomItem(config: Config) {
        currentNavigation.replaceCurrent(config)
    }

    override fun backPressedBottomItem() {

        val child = childStack.items[childStack.items.lastIndex - 1].instance

        when (child) {
            is MainComponent.Child.HomeChild -> {}
            is MainComponent.Child.CategoryChild -> navigateToBottomItem(Config.Category)
            is MainComponent.Child.BasketChild -> navigateToBottomItem(Config.Basket)
            is MainComponent.Child.FavoritesChild -> navigateToBottomItem(Config.Favorites)
            is MainComponent.Child.ProfileChild -> navigateToBottomItem(Config.Profile)
        }
    }

    override fun goToLogin() {
        goToLoginSelected()
    }

    private fun createChild(
        config: Config
    ): MainComponent.Child =
        when (config) {
            is Config.Home -> MainComponent.Child.HomeChild
            is Config.Category -> MainComponent.Child.CategoryChild
            is Config.Basket -> MainComponent.Child.BasketChild
            is Config.Favorites -> MainComponent.Child.FavoritesChild
            is Config.Profile -> MainComponent.Child.ProfileChild
        }

    private fun createChild(
        config: HomeConfig,
        componentContext: ComponentContext
    ): MainComponent.ChildHome =
        when (config) {
            HomeConfig.HomeScreen -> MainComponent.ChildHome.HomeChild(
                itemHome(componentContext)
            )
        }

    private fun createChild(
        config: CategoryConfig,
        componentContext: ComponentContext
    ): MainComponent.ChildCategory =
        when (config) {
            CategoryConfig.CategoryScreen -> MainComponent.ChildCategory.CategoryChild(
                itemCategory(componentContext)
            )
            CategoryConfig.SearchScreen -> MainComponent.ChildCategory.SearchChild(
                itemSearch(componentContext)
            )

            CategoryConfig.ListingScreen -> MainComponent.ChildCategory.ListingChild(
                itemListing(componentContext)
            )
        }

    private fun createChild(
        config: FavoritesConfig,
        componentContext: ComponentContext
    ): MainComponent.ChildFavorites =
        when (config) {
            FavoritesConfig.FavoritesScreen -> MainComponent.ChildFavorites.FavoritesChild(
                itemFavorites(componentContext)
            )
        }

    private fun createChild(
        config: BasketConfig,
        componentContext: ComponentContext
    ): MainComponent.ChildBasket =
        when (config) {
            BasketConfig.BasketScreen -> MainComponent.ChildBasket.BasketChild(
                itemBasket(componentContext)
            )
        }

    private fun createChild(
        config: ProfileConfig,
        componentContext: ComponentContext
    ): MainComponent.ChildProfile =
        when (config) {
            ProfileConfig.ProfileScreen -> MainComponent.ChildProfile.ProfileChild(
                itemProfile(componentContext)
            )
        }

    private fun itemProfile(componentContext: ComponentContext): ProfileComponent {
        return DefaultProfileComponent(
            componentContext = componentContext,
            navigation = modelNavigation.value.profileNavigation
        )
    }

    private fun itemBasket(componentContext: ComponentContext): market.engine.presentation.basket.BasketComponent {
        return market.engine.presentation.basket.DefaultBasketComponent(
            componentContext = componentContext,
            navigation = modelNavigation.value.basketNavigation
        )
    }

    private fun itemFavorites(componentContext: ComponentContext): FavoritesComponent {
        return DefaultFavoritesComponent(
            componentContext = componentContext,
            navigation = modelNavigation.value.favoritesNavigation
        )
    }

    private fun itemCategory(componentContext: ComponentContext): CategoryComponent {
        return DefaultCategoryComponent(
            componentContext = componentContext,
            goToListingSelected = {
                pushCatStack(CategoryScreenType.LISTING)
            },
            goToSearchSelected = {
                pushCatStack(CategoryScreenType.SEARCH)
            },
            onBackPressed = {
                pushCatStack(CategoryScreenType.CATEGORY)
            }
        )
    }

    private fun itemHome(componentContext: ComponentContext): HomeComponent {
        return DefaultHomeComponent(
            componentContext = componentContext,
            navigation = modelNavigation.value.homeNavigation,
            navigateToSearchSelected = {
                pushCatStack(CategoryScreenType.SEARCH)
                navigateToBottomItem(Config.Category)
            },
            navigateToListingSelected = {
                pushCatStack(CategoryScreenType.LISTING)
                navigateToBottomItem(Config.Category)
            }
        )
    }

    private fun itemSearch(componentContext: ComponentContext): SearchComponent {
        return DefaultSearchComponent(
            componentContext = componentContext,
            onBackPressed = {
                pushCatStack(searchData.value.categoryStack[searchData.value.categoryStack.lastIndex - 1])
            },
            goToListingSelected = {
                pushCatStack(CategoryScreenType.LISTING)
            },
            goToCategorySelected = {
                pushCatStack(CategoryScreenType.CATEGORY)
            }
        )
    }

    private fun itemListing(componentContext: ComponentContext): ListingComponent {
        return DefaultListingComponent(
            componentContext = componentContext,
            searchSelected = {
                pushCatStack(CategoryScreenType.SEARCH)
            },
            onBackPressed = {
                pushCatStack(CategoryScreenType.CATEGORY)
            }
        )
    }

    private fun pushCatStack(screenType: CategoryScreenType){
        when(screenType){
            CategoryScreenType.LISTING -> {
                searchData.value.categoryStack.remove(CategoryScreenType.LISTING)
                searchData.value.categoryStack.add(CategoryScreenType.LISTING)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.ListingScreen)
            }
            CategoryScreenType.SEARCH -> {
                searchData.value.categoryStack.remove(CategoryScreenType.SEARCH)
                searchData.value.categoryStack.add(CategoryScreenType.SEARCH)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.SearchScreen)
            }
            CategoryScreenType.CATEGORY -> {
                searchData.value.categoryStack.remove(CategoryScreenType.CATEGORY)
                searchData.value.categoryStack.add(CategoryScreenType.CATEGORY)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.CategoryScreen)
            }
        }
    }
}

@Serializable
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
}

@Serializable
sealed class HomeConfig {
    @Serializable
    data object HomeScreen : HomeConfig()
}

@Serializable
sealed class CategoryConfig {
    @Serializable
    data object CategoryScreen : CategoryConfig()

    @Serializable
    data object SearchScreen : CategoryConfig()

    @Serializable
    data object ListingScreen : CategoryConfig()
}

@Serializable
sealed class BasketConfig {
    @Serializable
    data object BasketScreen : BasketConfig()
}

@Serializable
sealed class FavoritesConfig {
    @Serializable
    data object FavoritesScreen : FavoritesConfig()
}

@Serializable
sealed class ProfileConfig {
    @Serializable
    data object ProfileScreen : ProfileConfig()
}


