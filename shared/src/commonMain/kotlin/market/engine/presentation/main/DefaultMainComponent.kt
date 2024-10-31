package market.engine.presentation.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.baseFilters.CategoryBaseFilters
import market.engine.core.globalData.UserData
import market.engine.core.items.DeepLink
import market.engine.core.navigation.children.ChildBasket
import market.engine.core.navigation.children.ChildCategory
import market.engine.core.navigation.children.ChildFavorites
import market.engine.core.navigation.children.ChildHome
import market.engine.core.navigation.children.ChildMain
import market.engine.core.navigation.children.ChildProfile
import market.engine.core.navigation.configs.BasketConfig
import market.engine.core.navigation.configs.CategoryConfig
import market.engine.core.navigation.configs.FavoritesConfig
import market.engine.core.navigation.configs.HomeConfig
import market.engine.core.navigation.configs.MainConfig
import market.engine.core.navigation.configs.ProfileConfig
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

class DefaultMainComponent(
    componentContext: ComponentContext,
    deepLink: DeepLink?,
    val goToLoginSelected: () -> Unit
) : MainComponent, ComponentContext by componentContext 
{

    override val categoryData: CategoryBaseFilters = getKoin().get()

    private val categoryStack = categoryData.categoryStack

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

    private var currentNavigation = StackNavigation<MainConfig>()

    override val childHomeStack: Value<ChildStack<*, ChildHome>> by lazy {
        childStack(
            source = modelNavigation.value.homeNavigation,
            initialConfiguration = HomeConfig.HomeScreen,
            serializer = HomeConfig.serializer(),
            
            childFactory = ::createChild,
            key = "HomeStack"
        )
    }

    override val childCategoryStack: Value<ChildStack<*, ChildCategory>> =
        childStack(
            source = modelNavigation.value.categoryNavigation,
            initialConfiguration = CategoryConfig.CategoryScreen,
            serializer = CategoryConfig.serializer(),
            
            childFactory = ::createChild,
            key = "CategoryStack"
        )


    override val childBasketStack: Value<ChildStack<*, ChildBasket>> by lazy {
        childStack(
            source = modelNavigation.value.basketNavigation,
            initialConfiguration = BasketConfig.BasketScreen,
            serializer = BasketConfig.serializer(),
            
            childFactory = ::createChild,
            key = "BasketStack"
        )
    }

    override val childFavoritesStack: Value<ChildStack<*, ChildFavorites>> by lazy {
        childStack(
            source = modelNavigation.value.favoritesNavigation,
            initialConfiguration = FavoritesConfig.FavoritesScreen,
            serializer = FavoritesConfig.serializer(),
            
            childFactory = ::createChild,
            key = "FavoritesStack"
        )
    }

    override val childProfileStack: Value<ChildStack<*, ChildProfile>> by lazy {
        childStack(
            source = modelNavigation.value.profileNavigation,
            initialConfiguration = ProfileConfig.ProfileScreen,
            serializer = ProfileConfig.serializer(),
            
            childFactory = ::createChild,
            key = "ProfileStack"
        )
    }


    override val childMainStack: Value<ChildStack<*, ChildMain>> =
        childStack(
            source = currentNavigation,
            serializer = MainConfig.serializer(),
            initialConfiguration = MainConfig.Home,
            
            childFactory = {config, _ ->
                createChild(config)
            },
            key = "MainStack"
        )

    init {
        deepLink?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(deepLink: DeepLink) {
        when (deepLink) {
            is DeepLink.User -> {
                navigateToBottomItem(MainConfig.Profile)
            }
            is DeepLink.Listing -> {
                categoryData.listingData.searchData.value.clear()
                categoryData.listingData.searchData.value.userSearch = true
                categoryData.listingData.searchData.value.userID = deepLink.ownerId
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.ListingScreen)
                navigateToBottomItem(MainConfig.Category)
            }
            is DeepLink.Offer -> {

            }
            is DeepLink.Auth -> {
                goToLogin()
            }
            is DeepLink.Registration -> {
                goToLogin()
            }
            is DeepLink.Unknown -> {}
        }
    }

    override fun navigateToBottomItem(config: MainConfig) {
        when(config){
            is MainConfig.Home -> {
                currentNavigation.replaceCurrent(config)
            }
            is MainConfig.Category -> {
                currentNavigation.replaceCurrent(config)
            }
            is MainConfig.Basket -> {
                if (UserData.token == "") {
                    goToLoginSelected()
                }else{
                    currentNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Favorites -> {
                if (UserData.token == "") {
                    goToLoginSelected()
                }else{
                    currentNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Profile -> {
                if (UserData.token == "") {
                    goToLoginSelected()
                }else{
                    currentNavigation.replaceCurrent(config)
                }
            }
        }
    }

    override fun goToLogin() {
        goToLoginSelected()
    }

    private fun createChild(
        config: MainConfig
    ): ChildMain =
        when (config) {
            is MainConfig.Home -> ChildMain.HomeChildMain
            is MainConfig.Category -> ChildMain.CategoryChildMain
            is MainConfig.Basket -> ChildMain.BasketChildMain
            is MainConfig.Favorites -> ChildMain.FavoritesChildMain
            is MainConfig.Profile -> ChildMain.ProfileChildMain
        }

    private fun createChild(
        config: HomeConfig,
        componentContext: ComponentContext
    ): ChildHome =
        when (config) {
            HomeConfig.HomeScreen -> ChildHome.HomeChild(
                itemHome(componentContext)
            )
        }

    private fun createChild(
        config: CategoryConfig,
        componentContext: ComponentContext
    ): ChildCategory =
        when (config) {
            CategoryConfig.CategoryScreen -> ChildCategory.CategoryChild(
                itemCategory(componentContext)
            )
            CategoryConfig.SearchScreen -> ChildCategory.SearchChild(
                itemSearch(componentContext)
            )

            CategoryConfig.ListingScreen -> ChildCategory.ListingChild(
                itemListing(componentContext)
            )
        }

    private fun createChild(
        config: FavoritesConfig,
        componentContext: ComponentContext
    ): ChildFavorites =
        when (config) {
            FavoritesConfig.FavoritesScreen -> ChildFavorites.FavoritesChild(
                itemFavorites(componentContext)
            )

            FavoritesConfig.SubscriptionsScreen -> ChildFavorites.SubChild(
                itemSubscriptions(componentContext)
            )
        }

    private fun createChild(
        config: BasketConfig,
        componentContext: ComponentContext
    ): ChildBasket =
        when (config) {
            BasketConfig.BasketScreen -> ChildBasket.BasketChild(
                itemBasket(componentContext)
            )
        }

    private fun createChild(
        config: ProfileConfig,
        componentContext: ComponentContext
    ): ChildProfile =
        when (config) {
            ProfileConfig.ProfileScreen -> ChildProfile.ProfileChild(
                itemProfile(componentContext)
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
                navigateToBottomItem(MainConfig.Category)
            },
            navigateToListingSelected = {
                pushCatStack(CategoryScreenType.LISTING)
                navigateToBottomItem(MainConfig.Category)
            }
        )
    }

    private fun itemSearch(componentContext: ComponentContext): SearchComponent {
        return DefaultSearchComponent(
            componentContext = componentContext,
            onBackPressed = {
                pushCatStack(categoryStack[categoryStack.lastIndex - 1])
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
        )
    }

    private fun itemSubscriptions(componentContext: ComponentContext): FavoritesComponent {
        return DefaultFavoritesComponent(
            componentContext = componentContext,
        )
    }


    private fun pushCatStack(screenType: CategoryScreenType){
        when(screenType){
            CategoryScreenType.LISTING -> {
                categoryStack.remove(CategoryScreenType.LISTING)
                categoryStack.add(CategoryScreenType.LISTING)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.ListingScreen)
            }
            CategoryScreenType.SEARCH -> {
                categoryStack.remove(CategoryScreenType.SEARCH)
                categoryStack.add(CategoryScreenType.SEARCH)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.SearchScreen)
            }
            CategoryScreenType.CATEGORY -> {
                categoryStack.remove(CategoryScreenType.CATEGORY)
                categoryStack.add(CategoryScreenType.CATEGORY)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.CategoryScreen)
            }
        }
    }
}
