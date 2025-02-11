package market.engine.fragments.root.main

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
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.FavScreenType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.basket.BasketConfig
import market.engine.fragments.root.main.basket.ChildBasket
import market.engine.fragments.root.main.home.ChildHome
import market.engine.fragments.root.main.home.HomeConfig
import market.engine.fragments.root.main.home.createHomeChild
import market.engine.fragments.root.main.favPages.ChildFavorites
import market.engine.fragments.root.main.favPages.FavoritesConfig
import market.engine.fragments.root.main.basket.createBasketChild
import market.engine.fragments.root.main.favPages.createFavoritesChild
import market.engine.fragments.root.main.profile.navigation.ChildProfile
import market.engine.fragments.root.main.profile.navigation.ProfileConfig
import market.engine.fragments.root.main.profile.navigation.createProfileChild
import market.engine.fragments.root.main.listing.ChildSearch
import market.engine.fragments.root.main.listing.SearchConfig
import market.engine.fragments.root.main.listing.createSearchChild

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

    val model : Value<Model>
    data class Model(
        val backHandler: BackHandler
    )

    fun navigateToBottomItem(config: MainConfig, openPage: String? = null)
    fun goToLogin(reset: Boolean = false)
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private var deepLink: DeepLink?,
    val goToLoginSelected: () -> Unit,
    val contactUsSelected: () -> Unit,
    val navigateToVerification: (String, Long?, String?) -> Unit,
    val navigateToDynamicSettings: (String, Long?, String?) -> Unit,

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

    private val _model = MutableValue(
        MainComponent.Model(
            backHandler = backHandler
        )
    )
    override val model = _model

    override val modelNavigation = _modelNavigation

    private var openPage: String? = null


    init {
        if(deepLink != null) {
            lifecycle.doOnResume {
                deepLink?.let { handleDeepLink(it) }
                deepLink = null
            }
        }
    }

    // Stacks
    override val childMainStack: Value<ChildStack<*, ChildMain>> =
        childStack(
            source = modelNavigation.value.mainNavigation,
            serializer = MainConfig.serializer(),
            handleBackButton = true,
            initialConfiguration = MainConfig.Home,
            childFactory = {config, _ ->
                createChild(config)
            },
            key = "MainStack"
        )

    override val childHomeStack: Value<ChildStack<*, ChildHome>> = childStack(
        source = modelNavigation.value.homeNavigation,
        initialConfiguration = HomeConfig.HomeScreen,
        serializer = HomeConfig.serializer(),
        handleBackButton = true,
        childFactory = { config, componentContext ->
            createHomeChild(
                config,
                componentContext,
                modelNavigation.value.homeNavigation,
                goToLoginSelected,
                navigateToMyOrders = { id, type ->
                    navigateToBottomItem(MainConfig.Profile, if(type == DealTypeGroup.BUY) "purchases/$id" else "sales/$id")
                },
                navigateToConversations = {
                    navigateToBottomItem(MainConfig.Profile, "conversations")
                },
                navigateToContactUs = {
                    contactUsSelected()
                },
                navigateToAppSettings = {
                    navigateToDynamicSettings("app_settings", null, null)
                },
                navigateToSubscribe = {
                    navigateToBottomItem(MainConfig.Favorites, "subscribe")
                }
            )
        },
        key = "HomeStack"
    )

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
            handleBackButton = true,
            childFactory = { config, componentContext ->
                createSearchChild(
                    config,
                    componentContext,
                    modelNavigation.value.searchNavigation,
                    navigateToMyOrders = { id, type ->
                        navigateToBottomItem(MainConfig.Profile, if(type == DealTypeGroup.BUY) "purchases/$id" else "sales/$id")
                    },
                    navigateToLogin = {
                        goToLogin()
                    },
                    navigateToSubscribe = {
                        navigateToBottomItem(MainConfig.Favorites, "subscribe")
                    },
                    navigateToConversations = {
                        navigateToBottomItem(MainConfig.Profile, "conversations")
                    }
                )
            },
            key = "SearchStack"
        )
    }

    override val childBasketStack: Value<ChildStack<*, ChildBasket>> by lazy {
        childStack(
            source = modelNavigation.value.basketNavigation,
            initialConfiguration = BasketConfig.BasketScreen,
            serializer = BasketConfig.serializer(),
            handleBackButton = true,
            childFactory = { config, componentContext ->
                createBasketChild(
                    config,
                    componentContext,
                    modelNavigation.value.basketNavigation,
                    navigateToMyOrders = { id, type ->
                        navigateToBottomItem(MainConfig.Profile, if(type == DealTypeGroup.BUY) "purchases/$id" else "sales/$id")
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToSubscribe = {
                        navigateToBottomItem(MainConfig.Favorites, "subscribe")
                    },
                    navigateToConversations = {
                        navigateToBottomItem(MainConfig.Profile, "conversations")
                    }
                )
            },
            key = "BasketStack"
        )
    }

    override val childFavoritesStack: Value<ChildStack<*, ChildFavorites>> by lazy {
        childStack(
            source = modelNavigation.value.favoritesNavigation,
            initialConfiguration = FavoritesConfig.FavPagesScreen(if(openPage == "subscribe") FavScreenType.SUBSCRIBED else FavScreenType.FAVORITES),
            serializer = FavoritesConfig.serializer(),
            handleBackButton = true,
            childFactory = { config, componentContext ->
                createFavoritesChild(
                    config,
                    componentContext,
                    modelNavigation.value.favoritesNavigation,
                    navigateToMyOrders = { id, type ->
                        navigateToBottomItem(MainConfig.Profile, if(type == DealTypeGroup.BUY) "purchases/$id" else "sales/$id")
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToConversations = {
                        navigateToBottomItem(MainConfig.Profile, "conversations")
                    }
                )
            },
            key = "FavoritesStack"
        )
    }

    override val childProfileStack: Value<ChildStack<*, ChildProfile>> by lazy {
        childStack(
            source = modelNavigation.value.profileNavigation,
            initialConfiguration = ProfileConfig.ProfileScreen(openPage = openPage),
            serializer = ProfileConfig.serializer(),
            handleBackButton = true,
            childFactory = { config, componentContext ->
                createProfileChild(
                    config,
                    componentContext,
                    modelNavigation.value.profileNavigation,
                    navigateToMyOrders = { id, type ->
                        navigateToBottomItem(MainConfig.Profile, if(type == DealTypeGroup.BUY) "purchases/$id" else "sales/$id")
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDynamicSettings = { settingsType ->
                        navigateToDynamicSettings(settingsType, null, null)
                    },
                    navigateToSubscribe = {
                        navigateToBottomItem(MainConfig.Favorites, "subscribe")
                    }
                )
            },
            key = "ProfileStack"
        )
    }

    val userInfo = UserData.userInfo

    // createChild
    private fun createChild(
        config: MainConfig,
        //componentContext: ComponentContext
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
            is DeepLink.GoToUser -> {
                modelNavigation.value.homeNavigation.pushNew(
                    HomeConfig.UserScreen(
                        deepLink.userId,
                        getCurrentDate(),
                        false
                    )
                )
            }
            is DeepLink.GoToListing -> {
                val categoryData = ListingData()
                categoryData.searchData.value.userSearch = true
                categoryData.searchData.value.userID = deepLink.ownerId
                categoryData.searchData.value.userLogin = ""

                modelNavigation.value.homeNavigation.pushNew(
                    HomeConfig.ListingScreen(
                        false,
                        categoryData.data.value,
                        categoryData.searchData.value,
                    )
                )
            }
            is DeepLink.GoToOffer -> {
                modelNavigation.value.homeNavigation.pushNew(
                    HomeConfig.OfferScreen(
                        deepLink.offerId,
                        getCurrentDate()
                    )
                )
            }
            is DeepLink.GoToAuth -> {
                if (UserData.token == "")
                    goToLogin()
            }
            is DeepLink.GoToRegistration -> {
                if (UserData.token == "")
                    goToLogin()
            }
            is DeepLink.GoToDynamicSettings -> {
                navigateToDynamicSettings(deepLink.settingsType, deepLink.ownerId, deepLink.code)
            }
            is DeepLink.GoToVerification -> {
                navigateToVerification(deepLink.settingsType ?: "", deepLink.ownerId, deepLink.code)
            }
            is DeepLink.Unknown -> {}
        }
    }

    override fun goToLogin(reset: Boolean) {
        goToLoginSelected()
        if (reset) {
            navigateToBottomItem(MainConfig.Home)
        }
    }

    private var activeCurrent = "Home"
    override fun navigateToBottomItem(
        config: MainConfig,
        openPage: String?
    ) {
        this.openPage = openPage
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
                    if(activeCurrent == "Basket"){
                        modelNavigation.value.basketNavigation.popToFirst()
                    }
                    activeCurrent = "Basket"
                    modelNavigation.value.mainNavigation.replaceCurrent(config)
                }
            }
            is MainConfig.Favorites -> {
                if (UserData.token == "") {
                    goToLogin()
                }else{
                    if(activeCurrent == "Favorites"){
                        modelNavigation.value.favoritesNavigation.popToFirst()
                    }
                    activeCurrent = "Favorites"

                    modelNavigation.value.mainNavigation.replaceCurrent(config)
                    if(openPage == "subscribe") {
                        modelNavigation.value.favoritesNavigation.replaceAll(
                            FavoritesConfig.FavPagesScreen(FavScreenType.SUBSCRIBED)
                        )
                    }
                }
            }
            is MainConfig.Profile -> {
                if (UserData.token == "") {
                    goToLogin()
                }else{
                    if(activeCurrent == "Profile"){
                        modelNavigation.value.profileNavigation.replaceAll(
                            ProfileConfig.ProfileScreen(openPage)
                        )
                    }
                    activeCurrent = "Profile"
                    modelNavigation.value.mainNavigation.replaceCurrent(config)
                    if(openPage != null) {
                        modelNavigation.value.profileNavigation.replaceAll(
                            ProfileConfig.ProfileScreen(openPage)
                        )
                    }
                }
            }
        }
    }
}
