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
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.FavScreenType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.basket.BasketConfig
import market.engine.fragments.root.main.basket.ChildBasket
import market.engine.fragments.root.main.home.ChildHome
import market.engine.fragments.root.main.home.HomeConfig
import market.engine.fragments.root.main.home.createHomeChild
import market.engine.fragments.root.main.favorites.ChildFavorites
import market.engine.fragments.root.main.favorites.FavoritesConfig
import market.engine.fragments.root.main.basket.createBasketChild
import market.engine.fragments.root.main.favorites.createFavoritesChild
import market.engine.fragments.root.main.favorites.pushFavStack
import market.engine.fragments.root.main.profile.navigation.ChildProfile
import market.engine.fragments.root.main.profile.navigation.ProfileConfig
import market.engine.fragments.root.main.profile.navigation.createProfileChild
import market.engine.fragments.root.main.listing.ChildSearch
import market.engine.fragments.root.main.listing.SearchConfig
import market.engine.fragments.root.main.listing.createSearchChild
import org.jetbrains.compose.resources.getString

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
    override val modelNavigation = _modelNavigation

    private var openPage: String? = null
    
    // Stacks
    override val childHomeStack: Value<ChildStack<*, ChildHome>> = childStack(
            source = modelNavigation.value.homeNavigation,
            initialConfiguration = HomeConfig.HomeScreen,
            serializer = HomeConfig.serializer(),
            childFactory = { config, componentContext ->
                createHomeChild(
                    config,
                    componentContext,
                    modelNavigation.value.homeNavigation,
                    goToMessenger = {
                        navigateToBottomItem(MainConfig.Profile, "messenger")
                    },
                    goToLoginSelected,
                    navigateToMyOrders = {
                        navigateToBottomItem(MainConfig.Profile, "purchases")
                    },
                    navigateToDialog = { dialogId ->
                        navigateToBottomItem(MainConfig.Profile, "conversations/$dialogId")
                    },
                    navigateToContactUs = {
                        contactUsSelected()
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
            childFactory = { config, componentContext ->
                createSearchChild(
                    config,
                    componentContext,
                    modelNavigation.value.searchNavigation,
                    navigateToMyOrders = {
                        navigateToBottomItem(MainConfig.Profile, "purchases")
                    },
                    navigateToLogin = {
                        goToLogin()
                    },
                    navigateToDialog = { dialogId ->
                        navigateToBottomItem(MainConfig.Profile, "conversations/$dialogId")
                    }
                )
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
                createBasketChild(
                    config,
                    componentContext,
                    modelNavigation.value.basketNavigation,
                    navigateToMyOrders = {
                        navigateToBottomItem(MainConfig.Profile, "purchases")
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDialog = { dialogId ->
                        navigateToBottomItem(MainConfig.Profile, "conversations/$dialogId")
                    }
                )
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
                createFavoritesChild(
                    config,
                    componentContext,
                    modelNavigation.value.favoritesNavigation,
                    navigateToMyOrders = {
                        navigateToBottomItem(MainConfig.Profile, "purchases")
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDialog = { dialogId ->
                        navigateToBottomItem(MainConfig.Profile, "conversations/$dialogId")
                    }
                )
            },
            key = "FavoritesStack"
        )
    }

    override val childProfileStack: Value<ChildStack<*, ChildProfile>> by lazy {
        childStack(
            source = modelNavigation.value.profileNavigation,
            initialConfiguration = ProfileConfig.ProfileScreen(openPage =openPage),
            serializer = ProfileConfig.serializer(),
            childFactory = { config, componentContext ->
                createProfileChild(
                    config,
                    componentContext,
                    modelNavigation.value.profileNavigation,
                    navigateToMyOrders = {
                        navigateToBottomItem(MainConfig.Profile, "purchases")
                    },
                    navigateToLogin = {
                        goToLogin(true)
                    },
                    navigateToDynamicSettings = { settingsType ->
                        navigateToDynamicSettings(settingsType, null, null)
                    }
                )
            },
            key = "ProfileStack"
        )
    }

    override val childMainStack: Value<ChildStack<*, ChildMain>> =
        childStack(
            source = modelNavigation.value.mainNavigation,
            serializer = MainConfig.serializer(),
            initialConfiguration = MainConfig.Home,
            childFactory = {config, componentContext ->
                createChild(config, componentContext)
            },
            key = "MainStack"
        )

    val userInfo = UserData.userInfo

    init {
        if(deepLink != null) {
            lifecycle.doOnResume {
                deepLink?.let { handleDeepLink(it) }
                deepLink = null
            }
        }
    }

    // createChild

    private fun createChild(
        config: MainConfig,
        componentContext: ComponentContext
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
                            ProfileConfig.ProfileScreen(openPage)
                        )
                    }
                    activeCurrent = "Profile"
                    modelNavigation.value.mainNavigation.replaceCurrent(config)
                }
            }
        }
    }
}
