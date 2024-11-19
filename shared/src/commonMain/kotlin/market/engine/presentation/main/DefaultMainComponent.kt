package market.engine.presentation.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
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
import market.engine.core.navigation.configs.MyOfferConfig
import market.engine.core.navigation.configs.ProfileConfig
import market.engine.core.types.CategoryScreenType
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType
import market.engine.core.types.ProfileScreenType
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
import market.engine.presentation.profileMyOffers.DefaultMyOffersComponent
import market.engine.presentation.profileMyOffers.MyOffersComponent
import market.engine.presentation.search.DefaultSearchComponent
import market.engine.presentation.search.SearchComponent
import market.engine.presentation.subscriptions.DefaultSubscribesComponent
import market.engine.presentation.subscriptions.SubscribesComponent
import org.koin.mp.KoinPlatform.getKoin

class DefaultMainComponent(
    componentContext: ComponentContext,
    deepLink: DeepLink?,
    val goToLoginSelected: () -> Unit,

    ) : MainComponent, ComponentContext by componentContext
{
    override val categoryStack = MutableValue(mutableListOf(CategoryScreenType.CATEGORY))
    override val favoritesStack = MutableValue(mutableListOf(FavScreenType.FAVORITES))
    override val profileStack = MutableValue(mutableListOf(ProfileScreenType.MY_OFFERS))

    override val mainViewModel = MutableValue<MainViewModel>(getKoin().get())

    private val categoryData = CategoryBaseFilters.filtersData

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

    private val navigationMyOffers = PagesNavigation<MyOfferConfig>()
    override val myOffersPages: Value<ChildPages<*, MyOffersComponent>> by lazy {
        childPages(
            source = navigationMyOffers,
            serializer = MyOfferConfig.serializer(),
            initialPages = {
                Pages(
                    listOf(
                        MyOfferConfig(type = LotsType.MYLOT_ACTIVE),
                        MyOfferConfig(type = LotsType.MYLOT_UNACTIVE),
                        MyOfferConfig(type = LotsType.MYLOT_FUTURE)
                    ),
                    selectedIndex = 0,
                )
            },
            key = "ProfileMyOffersStack",
            childFactory = ::itemMyOffers
        )
    }


    private fun itemMyOffers(config: MyOfferConfig,componentContext: ComponentContext): MyOffersComponent {
        return DefaultMyOffersComponent(
            componentContext = componentContext,
            type = config.type
        )
    }


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

    override val childProfileStack: Value<ChildStack<*, ChildProfile>> =
        childStack(
            source = modelNavigation.value.profileNavigation,
            initialConfiguration = ProfileConfig.ProfileScreen,
            serializer = ProfileConfig.serializer(),
            handleBackButton = true,
            childFactory = ::createChild,
            key = "ProfileStack"
        )

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
                categoryData.searchData.value.clear()
                categoryData.searchData.value.userSearch = true
                categoryData.searchData.value.userID = deepLink.ownerId
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

    private var activeCurrent = "Home"
    override fun navigateToBottomItem(config: MainConfig) {
        when(config){
            is MainConfig.Home -> {
                activeCurrent = "Home"
                currentNavigation.replaceCurrent(config)
            }
            is MainConfig.Category -> {
                activeCurrent = "Category"
                currentNavigation.replaceCurrent(config)
            }
            is MainConfig.Basket -> {
                if (UserData.token == "") {
                    goToLoginSelected()
                }else{
                    activeCurrent = "Basket"
                    currentNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Favorites -> {
                if (UserData.token == "") {
                    goToLoginSelected()
                }else{
                    activeCurrent = "Favorites"
                    currentNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Profile -> {
                if (UserData.token == "") {
                    goToLoginSelected()
                }else{
                    if(activeCurrent == "Profile"){
                        modelNavigation.value.profileNavigation.pop()
                    }
                    activeCurrent = "Profile"
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

            ProfileConfig.MyOffersScreen -> ChildProfile.MyOffersChild(
                component = this
            )
        }

    override fun selectMyOfferPage(type: LotsType) {
        when (type) {
            LotsType.MYLOT_ACTIVE -> {
                navigationMyOffers.select(0)
            }

            LotsType.MYLOT_UNACTIVE -> {
                navigationMyOffers.select(1)
            }

            LotsType.MYLOT_FUTURE -> {
                navigationMyOffers.select(2)
            }
            else -> {
                navigationMyOffers.select(0)
            }
        }
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
            },
            navigateToLoginSelected = {
                goToLogin()
            }
        )
    }

    private fun itemSearch(componentContext: ComponentContext): SearchComponent {
        return DefaultSearchComponent(
            componentContext = componentContext,
            onBackPressed = {
                pushCatStack(categoryStack.value[categoryStack.value.lastIndex - 1])
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

    @OptIn(DelicateDecomposeApi::class)
    private fun itemProfile(componentContext: ComponentContext): ProfileComponent {
        return DefaultProfileComponent(
            componentContext = componentContext,
            selectMyOffers = {
                profileStack.value.add(ProfileScreenType.MY_OFFERS)
                modelNavigation.value.profileNavigation.push(ProfileConfig.MyOffersScreen)
            }
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
            componentContext = componentContext
        ) {
            pushFavStack(FavScreenType.SUBSCRIBED)
        }
    }

    private fun itemSubscriptions(componentContext: ComponentContext): SubscribesComponent {
        return DefaultSubscribesComponent(
            componentContext = componentContext,
        ) {
            pushFavStack(FavScreenType.FAVORITES)
        }
    }

    private fun pushFavStack(screenType: FavScreenType){
        when(screenType){
            FavScreenType.FAVORITES -> {
                favoritesStack.value.remove(FavScreenType.SUBSCRIBED)
                favoritesStack.value.add(FavScreenType.FAVORITES)
                modelNavigation.value.favoritesNavigation.replaceCurrent(FavoritesConfig.FavoritesScreen)
            }
            FavScreenType.SUBSCRIBED -> {
                favoritesStack.value.remove(FavScreenType.FAVORITES)
                favoritesStack.value.add(FavScreenType.SUBSCRIBED)
                modelNavigation.value.favoritesNavigation.replaceCurrent(FavoritesConfig.SubscriptionsScreen)
            }
        }
    }
    private fun pushCatStack(screenType: CategoryScreenType){
        when(screenType){
            CategoryScreenType.LISTING -> {
                categoryStack.value.remove(CategoryScreenType.LISTING)
                categoryStack.value.add(CategoryScreenType.LISTING)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.ListingScreen)
            }
            CategoryScreenType.SEARCH -> {
                categoryStack.value.remove(CategoryScreenType.SEARCH)
                categoryStack.value.add(CategoryScreenType.SEARCH)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.SearchScreen)
            }
            CategoryScreenType.CATEGORY -> {
                categoryStack.value.remove(CategoryScreenType.CATEGORY)
                categoryStack.value.add(CategoryScreenType.CATEGORY)
                modelNavigation.value.categoryNavigation.replaceCurrent(CategoryConfig.CategoryScreen)
            }
        }
    }
}
