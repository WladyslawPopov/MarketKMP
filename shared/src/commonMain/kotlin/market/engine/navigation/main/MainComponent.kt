package market.engine.navigation.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.popToFirst
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.FavScreenType
import market.engine.navigation.main.children.BasketConfig
import market.engine.navigation.main.children.ChildBasket
import market.engine.navigation.main.children.ChildSearch
import market.engine.navigation.main.children.ChildHome
import market.engine.navigation.main.children.profile.ChildProfile
import market.engine.navigation.main.children.HomeConfig
import market.engine.navigation.main.children.createHomeChild
import market.engine.navigation.main.children.ChildFavorites
import market.engine.navigation.main.children.profile.ProfileConfig
import market.engine.navigation.main.children.profile.createProfileChild
import market.engine.navigation.main.children.SearchConfig
import market.engine.navigation.main.children.createSearchChild
import market.engine.navigation.main.children.FavoritesConfig
import market.engine.navigation.main.children.createBasketChild
import market.engine.navigation.main.children.createFavoritesChild
import market.engine.navigation.main.children.pushFavStack

interface MainComponent {

    val modelNavigation: Value<ModelNavigation>

    val childMainStack: Value<ChildStack<*, ChildMain>>

    val childHomeStack: Value<ChildStack<*, ChildHome>>

    val childSearchStack: Value<ChildStack<*, ChildSearch>>

    val childBasketStack: Value<ChildStack<*, ChildBasket>>

    val childFavoritesStack: Value<ChildStack<*, ChildFavorites>>

    val childProfileStack: Value<ChildStack<*, ChildProfile>>

    data class ModelNavigation(
        val mainNavigation : StackNavigation<MainConfig>,
        val homeNavigation : StackNavigation<HomeConfig>,
        val searchNavigation : StackNavigation<SearchConfig>,
        val basketNavigation : StackNavigation<BasketConfig>,
        val favoritesNavigation : StackNavigation<FavoritesConfig>,
        val profileNavigation : StackNavigation<ProfileConfig>,
    )

    fun navigateToBottomItem(config: MainConfig)
    fun goToLogin()
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    deepLink: DeepLink?,
    val goToLoginSelected: () -> Unit,
) : MainComponent, ComponentContext by componentContext {
    private val _modelNavigation = MutableValue(
        MainComponent.ModelNavigation(
            mainNavigation = StackNavigation(),
            homeNavigation = StackNavigation(),
            searchNavigation = StackNavigation(),
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
            childFactory = { config, componentContext ->
                createHomeChild(config, componentContext, modelNavigation.value.homeNavigation, goToLoginSelected)
            },
            key = "HomeStack"
        )
    }

    override val childSearchStack: Value<ChildStack<*, ChildSearch>> by lazy {
        val categoryData = ListingData()
        childStack(
            source = modelNavigation.value.searchNavigation,
            initialConfiguration = SearchConfig.ListingScreen(
                categoryData.data.value,
                categoryData.searchData.value,
                true
            ),
            serializer = SearchConfig.serializer(),
            childFactory = { config, componentContext ->
                createSearchChild(config, componentContext, modelNavigation.value.searchNavigation)
            },
            key = "CategoryStack"
        )
    }

    override val childBasketStack: Value<ChildStack<*, ChildBasket>> by lazy {
        childStack(
            source = modelNavigation.value.basketNavigation,
            initialConfiguration = BasketConfig.BasketScreen,
            serializer = BasketConfig.serializer(),
            childFactory = { config, componentContext ->
                createBasketChild(config, componentContext, modelNavigation.value.basketNavigation)
            },
            key = "BasketStack"
        )
    }

    override val childFavoritesStack: Value<ChildStack<*, ChildFavorites>> by lazy {
        childStack(
            source = modelNavigation.value.favoritesNavigation,
            initialConfiguration = FavoritesConfig.FavoritesScreen,
            serializer = FavoritesConfig.serializer(),
            childFactory = { config, componentContext ->
                createFavoritesChild(config, componentContext, modelNavigation.value.favoritesNavigation)
            },
            key = "FavoritesStack"
        )
    }

    override val childProfileStack: Value<ChildStack<*, ChildProfile>> by lazy {
        childStack(
            source = modelNavigation.value.profileNavigation,
            initialConfiguration = ProfileConfig.ProfileScreen,
            serializer = ProfileConfig.serializer(),
            childFactory = { config, componentContext ->
                createProfileChild(config, componentContext, modelNavigation.value.profileNavigation)
            },
            key = "ProfileStack"
        )
    }

    override val childMainStack: Value<ChildStack<*, ChildMain>> =
        childStack(
            source = modelNavigation.value.mainNavigation,
            serializer = MainConfig.serializer(),
            initialConfiguration = MainConfig.Home,
            childFactory = {config, _ ->
                createChild(config)
            },
            key = "MainStack"
        )

    val userInfo = UserData.userInfo

    init {
        deepLink?.let { handleDeepLink(it) }
    }

    // createChild

    private fun createChild(
        config: MainConfig
    ): ChildMain =
        when (config) {
            is MainConfig.Home -> ChildMain.HomeChildMain
            is MainConfig.Search -> ChildMain.CategoryChildMain
            is MainConfig.Basket -> ChildMain.BasketChildMain
            is MainConfig.Favorites -> ChildMain.FavoritesChildMain
            is MainConfig.Profile -> ChildMain.ProfileChildMain
        }
    
    private fun handleDeepLink(deepLink: DeepLink) {
        when (deepLink) {
            is DeepLink.User -> {
                navigateToBottomItem(
                    MainConfig.Profile
                )
            }
            is DeepLink.Listing -> {
                val categoryData = ListingData()
                categoryData.searchData.value.userSearch = true
                categoryData.searchData.value.userID = deepLink.ownerId
                modelNavigation.value.searchNavigation.pushNew(
                    SearchConfig.ListingScreen(
                        categoryData.data.value,
                        categoryData.searchData.value,
                        false
                    )
                )
                navigateToBottomItem(
                    MainConfig.Search
                )
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

    private var activeCurrent = "Home"
    override fun navigateToBottomItem(
        config: MainConfig
    ) {
        when(config){
            is MainConfig.Home -> {
                if(activeCurrent == "Home"){
                    modelNavigation.value.homeNavigation.popToFirst()
                }
                activeCurrent = "Home"
                modelNavigation.value.mainNavigation.replaceCurrent(config)
            }
            is MainConfig.Search -> {
                if(activeCurrent == "Category"){
                    val categoryData = ListingData()
                    modelNavigation.value.searchNavigation.replaceAll(
                        SearchConfig.ListingScreen(
                            categoryData.data.value,
                            categoryData.searchData.value,
                            true
                        )
                    )
                }
                activeCurrent = "Category"
                modelNavigation.value.mainNavigation.replaceAll(config)
            }
            is MainConfig.Basket -> {
                if (UserData.token == "") {
                    goToLogin()
                }else{
                    activeCurrent = "Basket"
                    modelNavigation.value.mainNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Favorites -> {
                if (UserData.token == "") {
                    goToLogin()
                }else{
                    if(activeCurrent == "Favorites"){
                        pushFavStack(
                            FavScreenType.FAVORITES,
                            favoritesNavigation = modelNavigation.value.favoritesNavigation)
                    }
                    activeCurrent = "Favorites"
                    modelNavigation.value.mainNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Profile -> {
                if (UserData.token == "") {
                    goToLogin()
                }else{
                    if(activeCurrent == "Profile"){
                        modelNavigation.value.profileNavigation.replaceAll(
                            ProfileConfig.ProfileScreen
                        )
                    }
                    activeCurrent = "Profile"
                    modelNavigation.value.mainNavigation.replaceCurrent(config)
                }
            }
        }
    }
}
