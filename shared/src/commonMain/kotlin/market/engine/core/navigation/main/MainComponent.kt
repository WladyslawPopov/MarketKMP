package market.engine.core.navigation.main

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
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.items.DeepLink
import market.engine.core.items.ListingData
import market.engine.core.items.NavigationItem
import market.engine.core.navigation.main.children.ChildBasket
import market.engine.core.navigation.main.children.ChildSearch
import market.engine.core.navigation.main.children.ChildHome
import market.engine.core.navigation.main.children.ChildProfile
import market.engine.core.navigation.main.configs.BasketConfig
import market.engine.core.navigation.main.configs.SearchConfig
import market.engine.core.navigation.main.configs.FavoritesConfig
import market.engine.core.navigation.main.configs.HomeConfig
import market.engine.core.navigation.main.configs.MainConfig
import market.engine.core.navigation.main.configs.MyOfferConfig
import market.engine.core.navigation.main.configs.ProfileConfig
import market.engine.core.navigation.main.children.ChildFavorites
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType
import market.engine.core.util.getCurrentDate
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
import market.engine.presentation.subscriptions.DefaultSubscribesComponent
import market.engine.presentation.subscriptions.SubscribesComponent
import market.engine.presentation.user.DefaultUserComponent
import market.engine.presentation.user.UserComponent

interface MainComponent {

    val modelNavigation: Value<ModelNavigation>

    val childMainStack: Value<ChildStack<*, ChildMain>>

    val childHomeStack: Value<ChildStack<*, ChildHome>>

    val childSearchStack: Value<ChildStack<*, ChildSearch>>

    val childBasketStack: Value<ChildStack<*, ChildBasket>>

    val childFavoritesStack: Value<ChildStack<*, ChildFavorites>>

    val childProfileStack: Value<ChildStack<*, ChildProfile>>

    val myOffersPages: Value<ChildPages<*, MyOffersComponent>>

    val profileNavigationList: Value<List<NavigationItem>>

    data class ModelNavigation(
        val homeNavigation : StackNavigation<HomeConfig>,
        val searchNavigation : StackNavigation<SearchConfig>,
        val basketNavigation : StackNavigation<BasketConfig>,
        val favoritesNavigation : StackNavigation<FavoritesConfig>,
        val profileNavigation : StackNavigation<ProfileConfig>,
    )

    fun selectMyOfferPage(type: LotsType)

    fun navigateToBottomItem(config: MainConfig)

    fun goToLogin()
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    deepLink: DeepLink?,
    val goToLoginSelected: () -> Unit,

    ) : MainComponent, ComponentContext by componentContext
{
    private var currentNavigation = StackNavigation<MainConfig>()
    private val navigationMyOffers = PagesNavigation<MyOfferConfig>()

    private val _modelNavigation = MutableValue(
        MainComponent.ModelNavigation(
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
            childFactory = ::createChild,
            key = "HomeStack"
        )
    }

    override val childSearchStack: Value<ChildStack<*, ChildSearch>> by lazy {
        val categoryData = ListingData()
        childStack(
            source = modelNavigation.value.searchNavigation,
            initialConfiguration = SearchConfig.ListingScreen(
                categoryData.data.value,
                categoryData.searchData.value
            ),
            serializer = SearchConfig.serializer(),
            childFactory = ::createChild,
            key = "CategoryStack"
        )
    }


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

    val userInfo = UserData.userInfo

    private val _profNav by lazy { MutableValue(listOf(
        NavigationItem(
            title = strings.createNewOfferTitle,
            icon = drawables.newLotIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = null,
            onClick = {

            }
        ),
        NavigationItem(
            title = strings.myBidsTitle,
            subtitle = strings.myBidsSubTitle,
            icon = drawables.bidsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.proposalTitle,
            subtitle = strings.proposalPriceSubTitle,
            icon = drawables.proposalIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = if((userInfo?.countUnreadPriceProposals ?:0) > 0)
                userInfo?.countUnreadPriceProposals else null
        ),
        NavigationItem(
            title = strings.myPurchasesTitle,
            subtitle = strings.myPurchasesSubTitle,
            icon = drawables.purchasesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.myOffersTitle,
            subtitle = strings.myOffersSubTitle,
            icon = drawables.tagIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                try {
                    modelNavigation.value.profileNavigation.replaceCurrent(
                        ProfileConfig.MyOffersScreen
                    )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = strings.mySalesTitle,
            subtitle = strings.mySalesSubTitle,
            icon = drawables.salesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.messageTitle,
            subtitle = strings.messageSubTitle,
            icon = drawables.dialogIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = userInfo?.countUnreadMessages
        ),
        NavigationItem(
            title = strings.myProfileTitle,
            subtitle =strings.myProfileSubTitle,
            icon = drawables.profileIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                try {
                    modelNavigation.value.profileNavigation.pushNew(
                        ProfileConfig.UserScreen(
                            UserData.login,
                            getCurrentDate(),
                            false
                        )
                    )
                } catch ( _ : Exception){}
            }
        ),
        NavigationItem(
            title = strings.settingsProfileTitle,
            subtitle = strings.profileSettingsSubTitle,
            icon = drawables.settingsIcon,
            tint = colors.black,
            hasNews = true,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.myBalanceTitle,
            subtitle = strings.myBalanceSubTitle,
            icon = drawables.balanceIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = strings.logoutTitle,
            icon = drawables.logoutIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {

            }
        ),
    )) }

    override val profileNavigationList by lazy { _profNav }

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
                    },
                    onListingSelected = {
                        modelNavigation.value.homeNavigation.pushNew(
                            HomeConfig.ListingScreen(false, it.data.value, it.searchData.value)
                        )
                    },
                    onUserSelected = { ui, about ->
                        modelNavigation.value.homeNavigation.pushNew(
                            HomeConfig.UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    isSnapshot = config.isSnapshot
                )
            )
            is HomeConfig.ListingScreen -> {
                val ld = ListingData(
                    _searchData = config.searchData,
                    _data = config.listingData
                )
                ChildHome.ListingChild(
                    component = itemListing(
                        componentContext,
                        ld,
                        selectOffer = {
                            modelNavigation.value.homeNavigation.pushNew(
                                HomeConfig.OfferScreen(it, getCurrentDate())
                            )
                        },
                        onBack = {
                            modelNavigation.value.homeNavigation.pop()
                        },
                        isOpenCategory = false,
                        isOpenSearch = config.isOpenSearch
                    ),
                )
            }

            is HomeConfig.UserScreen -> ChildHome.UserChild(
                component = itemUser(
                    componentContext,
                    config.userId,
                    config.aboutMe,
                    goToLogin = {
                        modelNavigation.value.homeNavigation.pushNew(
                            HomeConfig.ListingScreen(false, it.data.value, it.searchData.value)
                        )
                    },
                    goBack = {
                        modelNavigation.value.homeNavigation.pop()
                    },
                    goToSnapshot = { id->
                       modelNavigation.value.homeNavigation.pushNew(
                           HomeConfig.OfferScreen(id, getCurrentDate(), true)
                       )
                    },
                    goToUser = {
                        modelNavigation.value.homeNavigation.pushNew(
                            HomeConfig.UserScreen(it, getCurrentDate(), false)
                        )
                    }
                )
            )
        }

    //Search
    private fun createChild(
        config: SearchConfig,
        componentContext: ComponentContext
    ): ChildSearch =
        when (config) {
            is SearchConfig.ListingScreen -> {
                val ld = ListingData(
                    _searchData = config.searchData,
                    _data = config.listingData
                )

                ChildSearch.ListingChild(
                    component = itemListing(
                        componentContext,
                        ld,
                        selectOffer = {
                            modelNavigation.value.searchNavigation.pushNew(
                                SearchConfig.OfferScreen(
                                    it,
                                    getCurrentDate()
                                )
                            )
                        },
                        onBack = {
                            modelNavigation.value.searchNavigation.pop()
                        },
                        isOpenCategory = false,
                        isOpenSearch = true
                    ),
                )
            }
            is SearchConfig.OfferScreen -> ChildSearch.OfferChild(
                component = itemOffer(
                    componentContext,
                    config.id,
                    selectOffer = {
                        modelNavigation.value.searchNavigation.pushNew(
                            SearchConfig.OfferScreen(
                                it,
                                getCurrentDate(),
                            )
                        )
                    },
                    onBack = {
                        modelNavigation.value.searchNavigation.pop()
                    },
                    onListingSelected = {
                        modelNavigation.value.searchNavigation.pushNew(
                            SearchConfig.ListingScreen(it.data.value, it.searchData.value)
                        )
                    },
                    onUserSelected = { ui, about ->
                        modelNavigation.value.searchNavigation.pushNew(
                            SearchConfig.UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    config.isSnapshot
                )
            )
            is SearchConfig.UserScreen -> ChildSearch.UserChild(
                component = itemUser(
                    componentContext,
                    config.id,
                    config.aboutMe,
                    goToLogin = {
                        modelNavigation.value.searchNavigation.pushNew(
                            SearchConfig.ListingScreen(it.data.value, it.searchData.value)
                        )
                    },
                    goBack = {
                        modelNavigation.value.searchNavigation.pop()
                    },
                    goToSnapshot = { id ->
                        modelNavigation.value.searchNavigation.pushNew(
                            SearchConfig.OfferScreen(id, getCurrentDate(), true)
                        )
                    },
                    goToUser = {
                        modelNavigation.value.searchNavigation.pushNew(
                            SearchConfig.UserScreen(it, getCurrentDate(), false)
                        )
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
                            FavoritesConfig.OfferScreen(it, getCurrentDate())
                        )
                    },
                    onBack = {
                        modelNavigation.value.favoritesNavigation.pop()
                    },
                    onListingSelected = {
                        modelNavigation.value.favoritesNavigation.pushNew(
                            FavoritesConfig.ListingScreen(it.data.value, it.searchData.value)
                        )
                    },
                    onUserSelected = { ui, about ->
                        modelNavigation.value.favoritesNavigation.pushNew(
                            FavoritesConfig.UserScreen(ui, getCurrentDate(), about)
                        )
                    },
                    isSnapshot = config.isSnap
                )
            )

            is FavoritesConfig.ListingScreen -> {
                val ld = ListingData(
                    _searchData = config.searchData,
                    _data = config.listingData
                )
                ChildFavorites.ListingChild(
                    component = itemListing(
                        componentContext,
                        ld,
                        selectOffer = {
                            modelNavigation.value.favoritesNavigation.pushNew(
                                FavoritesConfig.OfferScreen(it, getCurrentDate())
                            )
                        },
                        onBack = {
                            modelNavigation.value.favoritesNavigation.pop()
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
                        modelNavigation.value.favoritesNavigation.pushNew(
                            FavoritesConfig.ListingScreen(it.data.value, it.searchData.value)
                        )
                    },
                    goBack = {
                        modelNavigation.value.favoritesNavigation.pop()
                    },
                    goToSnapshot = { id->
                        modelNavigation.value.favoritesNavigation.pushNew(
                            FavoritesConfig.OfferScreen(id, getCurrentDate(), true)
                        )
                    },
                    goToUser = {
                        modelNavigation.value.favoritesNavigation.pushNew(
                            FavoritesConfig.UserScreen(it, getCurrentDate(),false)
                        )
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
                component =  itemOffer(
                    componentContext,
                    config.id,
                    selectOffer = {
                        modelNavigation.value.profileNavigation.pushNew(ProfileConfig.OfferScreen(it, getCurrentDate()))
                    },
                    onBack = {
                        modelNavigation.value.profileNavigation.pop()
                    },
                    onListingSelected = { ld ->
                        modelNavigation.value.profileNavigation.pushNew(ProfileConfig.ListingScreen(ld.data.value, ld.searchData.value))
                    },
                    onUserSelected = { ui, about ->
                        modelNavigation.value.profileNavigation.pushNew(ProfileConfig.UserScreen(ui, getCurrentDate(), about))
                    },
                    isSnapshot = config.isSnapshot
                )
            )

            is ProfileConfig.ListingScreen -> {
                val ld = ListingData(
                    _searchData = config.searchData,
                    _data = config.listingData
                )
                ChildProfile.ListingChild(
                    component = itemListing(
                        componentContext,
                        ld,
                        selectOffer = {
                            modelNavigation.value.profileNavigation.pushNew(
                                ProfileConfig.OfferScreen(it, getCurrentDate())
                            )
                        },
                        onBack = {
                            modelNavigation.value.profileNavigation.pop()
                        },
                        isOpenCategory = false,
                    )
                )
            }

            is ProfileConfig.UserScreen -> {
                ChildProfile.UserChild(
                    component = itemUser(
                        componentContext,
                        config.userId,
                        config.aboutMe,
                        goToLogin = {
                            modelNavigation.value.profileNavigation.pushNew(
                                ProfileConfig.ListingScreen(it.data.value, it.searchData.value)
                            )
                        },
                        goBack = {
                            modelNavigation.value.profileNavigation.pop()
                        },
                        goToSnapshot = { id->
                            modelNavigation.value.profileNavigation.pushNew(
                                ProfileConfig.OfferScreen(id, getCurrentDate(), true)
                            )
                        },
                        goToUser = {
                            modelNavigation.value.profileNavigation.pushNew(
                                ProfileConfig.UserScreen(it, getCurrentDate(), false)
                            )
                        }
                    )
                )
            }
        }


    // Items

    private fun itemHome(componentContext: ComponentContext): HomeComponent {
        return DefaultHomeComponent(
            componentContext = componentContext,
            navigation = modelNavigation.value.homeNavigation,
            navigateToListingSelected = { ld, isNewSearch ->
                modelNavigation.value.homeNavigation.pushNew(HomeConfig.ListingScreen(isNewSearch, ld.data.value, ld.searchData.value))
            },
            navigateToLoginSelected = {
                goToLogin()
            },
            navigateToOfferSelected = { id ->
                modelNavigation.value.homeNavigation.pushNew(HomeConfig.OfferScreen(id, getCurrentDate()))
            }
        )
    }

    private fun itemListing(
        componentContext: ComponentContext,
        listingData: ListingData,
        selectOffer: (Long) -> Unit,
        onBack : () -> Unit,
        isOpenSearch : Boolean = false,
        isOpenCategory : Boolean
    ): ListingComponent {
        return DefaultListingComponent(
            isOpenSearch = isOpenSearch,
            isOpenCategory = isOpenCategory,
            componentContext = componentContext,
            listingData = listingData,
            selectOffer = { id ->
                selectOffer(id)
            },
            selectedBack = {
                onBack()
            },
        )
    }

    private fun itemOffer(
        componentContext: ComponentContext,
        id: Long,
        selectOffer: (Long) -> Unit,
        onBack : () -> Unit,
        onListingSelected: (ListingData) -> Unit,
        onUserSelected: (Long, Boolean) -> Unit,
        isSnapshot: Boolean = false,
    ): OfferComponent {
        return DefaultOfferComponent(
            id,
            isSnapshot,
            componentContext,
            selectOffer = { newId->
                selectOffer(newId)
            },
            navigationBack = {
                onBack()
            },
            navigationListing = {
                onListingSelected(it)
            },
            navigationBasket = {
                navigateToBottomItem(MainConfig.Basket)
            },
            navigateToUser = { ui, about ->
                onUserSelected(ui, about)
            },
        )
    }

    private fun itemProfile(componentContext: ComponentContext): ProfileComponent {
        return DefaultProfileComponent(
            componentContext = componentContext,
            navigationItems = profileNavigationList.value,
            selectMyOffers = {
                modelNavigation.value.profileNavigation.pushNew(ProfileConfig.MyOffersScreen)
            },
            navigateToUser = { id, aboutMe ->
                modelNavigation.value.profileNavigation.pushNew(
                    ProfileConfig.UserScreen(id, getCurrentDate(), aboutMe)
                )
            },
            navigateToListing = {
                modelNavigation.value.profileNavigation.pushNew(
                    ProfileConfig.ListingScreen(it.data.value, it.searchData.value)
                )
            }
        )
    }

    private fun itemUser(
        componentContext: ComponentContext,
        userId: Long,
        isClickedAboutMe: Boolean,
        goToLogin: (ListingData) -> Unit,
        goBack: () -> Unit,
        goToSnapshot: (Long) -> Unit,
        goToUser: (Long) -> Unit,
    ): UserComponent {
        return DefaultUserComponent(
            userId = userId,
            isClickedAboutMe = isClickedAboutMe,
            componentContext = componentContext,
            goToListing = goToLogin,
            navigateBack = goBack,
            navigateToOrder = {

            },
            navigateToSnapshot = goToSnapshot,
            navigateToUser = goToUser
        )
    }


    private fun itemMyOffers(config: MyOfferConfig, componentContext: ComponentContext): MyOffersComponent {
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
                    val categoryData = ListingData()
                    modelNavigation.value.searchNavigation.replaceAll(
                        SearchConfig.ListingScreen(
                            categoryData.data.value,
                            categoryData.searchData.value
                        )
                    )
                }
                activeCurrent = "Category"
                currentNavigation.replaceAll(config)
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
                        modelNavigation.value.profileNavigation.replaceAll(
                            ProfileConfig.ProfileScreen
                        )
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
                val categoryData = ListingData()
                categoryData.searchData.value.userSearch = true
                categoryData.searchData.value.userID = deepLink.ownerId
                modelNavigation.value.searchNavigation.pushNew(SearchConfig.ListingScreen(categoryData.data.value, categoryData.searchData.value))
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
