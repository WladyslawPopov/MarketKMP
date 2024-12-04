package market.engine.presentation.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popToFirst
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.filtersObjects.CategoryBaseFilters
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
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType
import market.engine.core.types.ProfileScreenType
import market.engine.core.util.getCurrentDate
import market.engine.presentation.category.CategoryComponent
import market.engine.presentation.category.DefaultCategoryComponent
import market.engine.presentation.favorites.DefaultFavoritesComponent
import market.engine.presentation.favorites.FavoritesComponent
import market.engine.presentation.home.DefaultHomeComponent
import market.engine.presentation.home.HomeComponent
import market.engine.presentation.listing.DefaultListingComponent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.offer.DefaultOfferComponent
import market.engine.presentation.offer.OfferComponent
import market.engine.presentation.profile.DefaultProfileComponent
import market.engine.presentation.profile.ProfileComponent
import market.engine.presentation.profileMyOffers.DefaultMyOffersComponent
import market.engine.presentation.profileMyOffers.MyOffersComponent
import market.engine.presentation.search.DefaultSearchComponent
import market.engine.presentation.search.SearchComponent
import market.engine.presentation.subscriptions.DefaultSubscribesComponent
import market.engine.presentation.subscriptions.SubscribesComponent

class DefaultMainComponent(
    componentContext: ComponentContext,
    deepLink: DeepLink?,
    val goToLoginSelected: () -> Unit,

    ) : MainComponent, ComponentContext by componentContext
{
    override val homeStack: Value<MutableList<HomeConfig>> = MutableValue(
        mutableListOf(HomeConfig.HomeScreen)
    )
    override val basketStack: Value<MutableList<BasketConfig>> = MutableValue(mutableListOf(BasketConfig.BasketScreen))
    override val categoryStack: Value<MutableList<CategoryConfig>> = MutableValue(mutableListOf(CategoryConfig.CategoryScreen(1L)))
    override val favoritesStack = MutableValue(mutableListOf(FavScreenType.FAVORITES))
    override val profileStack = MutableValue(mutableListOf(ProfileScreenType.MY_OFFERS))

    private var currentNavigation = StackNavigation<MainConfig>()
    private val navigationMyOffers = PagesNavigation<MyOfferConfig>()

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


    // Stacks
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
            initialConfiguration = CategoryConfig.CategoryScreen(1L),
            serializer = CategoryConfig.serializer(),
            childFactory = ::createChild,
            key = "CategoryStack"
        )




    override val myOffersPages: Value<ChildPages<*, MyOffersComponent>> by lazy {
        childPages(
            source = navigationMyOffers,
            serializer = MyOfferConfig.serializer(),
            handleBackButton = true,
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

    // createChild

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

            is HomeConfig.OfferScreen -> ChildHome.OfferChild(
                component = itemOffer(
                    componentContext,
                    config.id,
                    selectOffer = {
                        val offerConfig = HomeConfig.OfferScreen(it, getCurrentDate())
                        modelNavigation.value.homeNavigation.pushNew(offerConfig)
                    },
                    onBack = {
                        modelNavigation.value.homeNavigation.pop()
                    }
                )
            )
        }

    private fun createChild(
        config: CategoryConfig,
        componentContext: ComponentContext
    ): ChildCategory =
        when (config) {
            is CategoryConfig.CategoryScreen -> ChildCategory.CategoryChild(
                itemCategory(
                    componentContext
                )
            )
            CategoryConfig.SearchScreen -> ChildCategory.SearchChild(
                itemSearch(componentContext)
            )

            CategoryConfig.ListingScreen -> ChildCategory.ListingChild(
                itemListing(componentContext)
            )
            is CategoryConfig.OfferScreen -> ChildCategory.OfferChild(
                component = itemOffer(
                    componentContext,
                    config.id,
                    selectOffer = {
                        navigateToOrPopUntil(CategoryConfig.OfferScreen(it, getCurrentDate()))
                    },
                    onBack = {
                        modelNavigation.value.categoryNavigation.pop()
                        categoryStack.value.removeLastOrNull()
                    }
                )
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

            is FavoritesConfig.OfferScreen -> ChildFavorites.OfferChild(
                component = itemOffer(
                    componentContext,
                    config.id,
                    selectOffer = {
                        modelNavigation.value.favoritesNavigation.pushNew(
                            FavoritesConfig.OfferScreen(it, getCurrentDate()
                            )
                        )
                    },
                    onBack = {
                        modelNavigation.value.favoritesNavigation.pop()
                    }
                )
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

            is ProfileConfig.OfferScreen -> ChildProfile.OfferChild(
                component = itemOffer(
                    componentContext,
                    config.id,
                    selectOffer = {
                        modelNavigation.value.profileNavigation.pushNew(ProfileConfig.OfferScreen(it, getCurrentDate()))
                    },
                    onBack = {
                        modelNavigation.value.profileNavigation.pop()
                    }
                )
            )
        }

    // Items

    private fun itemHome(componentContext: ComponentContext): HomeComponent {
        return DefaultHomeComponent(
            componentContext = componentContext,
            navigation = modelNavigation.value.homeNavigation,
            navigateToSearchSelected = {
                navigateToBottomItem(MainConfig.Category)

                navigateToOrPopUntil(
                    CategoryConfig.SearchScreen
                )
            },
            navigateToListingSelected = {
                navigateToBottomItem(MainConfig.Category)

                navigateToOrPopUntil(
                    CategoryConfig.ListingScreen
                )
            },
            navigateToLoginSelected = {
                goToLogin()
            },
            navigateToOfferSelected = { id ->
                modelNavigation.value.homeNavigation.pushNew(HomeConfig.OfferScreen(id, getCurrentDate()))
            }
        )
    }

    private fun itemCategory(componentContext: ComponentContext): CategoryComponent {
        return DefaultCategoryComponent(
            componentContext = componentContext,
            goToListingSelected = {
                navigateToOrPopUntil(
                    CategoryConfig.ListingScreen
                )
            },
            goToSearchSelected = {
                navigateToOrPopUntil(
                    CategoryConfig.SearchScreen
                )
            },
            goToNewCategory = {
                navigateToOrPopUntil(
                    CategoryConfig.CategoryScreen(categoryData.searchData.value.searchCategoryID ?: 1)
                )
            },
            onBackClicked = {
                categoryStack.value.removeLastOrNull()
                modelNavigation.value.categoryNavigation.pop()
            }
        )
    }

    private fun itemSearch(componentContext: ComponentContext): SearchComponent {
        return DefaultSearchComponent(
            componentContext = componentContext,
            onBackPressed = {
                categoryStack.value.removeLast()
                modelNavigation.value.categoryNavigation.pop()
            },
            goToListingSelected = {
                navigateToOrPopUntil(CategoryConfig.ListingScreen, true)
            },
            goToCategorySelected = {
                navigateToOrPopUntil(CategoryConfig.CategoryScreen(categoryData.searchData.value.searchCategoryID ?: 1L))
            }
        )
    }

    private fun itemListing(componentContext: ComponentContext): ListingComponent {
        return DefaultListingComponent(
            componentContext = componentContext,
            searchSelected = {
                navigateToOrPopUntil(CategoryConfig.SearchScreen)
            },
            selectOffer = { id ->
                navigateToOrPopUntil(CategoryConfig.OfferScreen(id, getCurrentDate()))
            },
            onBackPressed = {
                categoryStack.value.removeLast()
                modelNavigation.value.categoryNavigation.pop()
            }
        )
    }
    private fun itemOffer(componentContext: ComponentContext, id: Long, selectOffer: (Long) -> Unit, onBack : () -> Unit): OfferComponent {
        return DefaultOfferComponent(
            id,
            false,
            componentContext,
            selectOffer = { newId->
                selectOffer(newId)
            },
            navigationBack = {
                onBack()
            }
        )
    }

    private fun itemProfile(componentContext: ComponentContext): ProfileComponent {
        return DefaultProfileComponent(
            componentContext = componentContext,
            selectMyOffers = {
                profileStack.value.add(ProfileScreenType.MY_OFFERS)
                modelNavigation.value.profileNavigation.pushNew(ProfileConfig.MyOffersScreen)
            }
        )
    }


    private fun itemMyOffers(config: MyOfferConfig,componentContext: ComponentContext): MyOffersComponent {
        return DefaultMyOffersComponent(
            componentContext = componentContext,
            type = config.type,
            offerSelected = { id ->
                modelNavigation.value.profileNavigation.pushNew(ProfileConfig.OfferScreen(id, getCurrentDate()))
            },
            selectedMyOfferPage = { type ->
                selectMyOfferPage(type)
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
            componentContext = componentContext,
            goToOffer = { id ->
                pushFavStack(FavScreenType.OFFER, id)
            },
            selectedSubscribes = {
                pushFavStack(FavScreenType.SUBSCRIBED)
            }
        )
    }

    private fun itemSubscriptions(componentContext: ComponentContext): SubscribesComponent {
        return DefaultSubscribesComponent(
            componentContext = componentContext,
        ) {
            pushFavStack(FavScreenType.FAVORITES)
        }
    }

    private fun navigateToOrPopUntil(screenConfig: CategoryConfig, isReplace : Boolean = false) {
        val stack = categoryStack.value
        if (stack.lastOrNull() != screenConfig) {
            if (stack.contains(screenConfig)) {
                while (stack.lastOrNull() != screenConfig) {
                    modelNavigation.value.categoryNavigation.pop()
                    stack.removeLast()
                }
            } else {
                if (isReplace) {
                    stack.removeLastOrNull()
                    stack.add(screenConfig)
                    modelNavigation.value.categoryNavigation.replaceCurrent(screenConfig)
                }else {
                    stack.add(screenConfig)
                    modelNavigation.value.categoryNavigation.pushNew(screenConfig)
                }
            }
        }
    }

    private fun pushFavStack(screenType: FavScreenType, id: Long = 1L){
        when(screenType){
            FavScreenType.FAVORITES -> {
                modelNavigation.value.favoritesNavigation.replaceAll(FavoritesConfig.FavoritesScreen)
            }
            FavScreenType.SUBSCRIBED -> {
                modelNavigation.value.favoritesNavigation.replaceAll(FavoritesConfig.SubscriptionsScreen)
            }
            FavScreenType.OFFER -> {
                modelNavigation.value.favoritesNavigation.pushNew(FavoritesConfig.OfferScreen(id, getCurrentDate()))
            }
        }
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

    private var activeCurrent = "Home"
    override fun navigateToBottomItem(config: MainConfig) {
        when(config){
            is MainConfig.Home -> {
                if(activeCurrent == "Home"){
                    modelNavigation.value.homeNavigation.popToFirst()
                }
                activeCurrent = "Home"
                currentNavigation.replaceCurrent(config)
            }
            is MainConfig.Category -> {
                if(activeCurrent == "Category"){
                    categoryData.searchData.value.clear()
                    categoryData.searchData.value.clearCategory()
                    categoryData.data.value.clearFilters()
                    categoryStack.value.clear()
                    categoryStack.value.add(CategoryConfig.CategoryScreen(1L))
                    modelNavigation.value.categoryNavigation.replaceAll(categoryStack.value.last())
                }
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
                    if(activeCurrent == "Favorites"){
                        pushFavStack(FavScreenType.FAVORITES)
                    }
                    activeCurrent = "Favorites"
                    currentNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Profile -> {
                if (UserData.token == "") {
                    goToLoginSelected()
                }else{
                    if(activeCurrent == "Profile"){
                        modelNavigation.value.profileNavigation.popToFirst()
                    }
                    activeCurrent = "Profile"
                    currentNavigation.replaceCurrent(config)
                }
            }
        }
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
    override fun goToLogin() {
        goToLoginSelected()
    }
}
