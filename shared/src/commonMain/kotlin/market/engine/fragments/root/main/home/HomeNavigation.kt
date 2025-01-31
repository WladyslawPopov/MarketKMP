package market.engine.fragments.root.main.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.fragments.root.main.user.userFactory
import market.engine.core.data.types.CreateOfferType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.createOffer.CreateOfferComponent
import market.engine.fragments.root.main.createOffer.CreateOfferContent
import market.engine.fragments.root.main.createOffer.createOfferFactory
import market.engine.fragments.root.main.createOrder.CreateOrderComponent
import market.engine.fragments.root.main.createOrder.CreateOrderContent
import market.engine.fragments.root.main.createOrder.createOrderFactory
import market.engine.fragments.root.main.listing.ListingComponent
import market.engine.fragments.root.main.listing.ListingContent
import market.engine.fragments.root.main.listing.listingFactory
import market.engine.fragments.root.main.offer.OfferComponent
import market.engine.fragments.root.main.offer.OfferContent
import market.engine.fragments.root.main.offer.offerFactory
import market.engine.fragments.root.main.user.UserComponent
import market.engine.fragments.root.main.user.UserContent

@Serializable
sealed class HomeConfig {
    @Serializable
    data object HomeScreen : HomeConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : HomeConfig()

    @Serializable
    data class ListingScreen(val isOpenSearch : Boolean, val listingData: LD, val searchData : SD) : HomeConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe : Boolean) : HomeConfig()

    @Serializable
    data class CreateOfferScreen(
        val catPath: List<Long>? = null,
        val offerId: Long? = null,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>? = null
    ) : HomeConfig()

    @Serializable
    data class CreateOrderScreen(
        val basketItem : Pair<Long, List<SelectedBasketItem>>,
    ) : HomeConfig()
}

sealed class ChildHome {
    class HomeChild(val component: HomeComponent) : ChildHome()
    class OfferChild(val component: OfferComponent) : ChildHome()
    class ListingChild(val component: ListingComponent) : ChildHome()
    class UserChild(val component: UserComponent) : ChildHome()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildHome()
    class CreateOrderChild(val component: CreateOrderComponent) : ChildHome()
}

@Composable
fun HomeNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildHome>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildHome.HomeChild ->{
                HomeContent(screen.component, modifier)
            }
            is ChildHome.OfferChild ->{
                OfferContent(screen.component, modifier)
            }
            is ChildHome.ListingChild ->{
                ListingContent(screen.component, modifier)
            }
            is ChildHome.UserChild ->{
                UserContent(screen.component, modifier)
            }
            is ChildHome.CreateOfferChild ->{
                CreateOfferContent(screen.component)
            }
            is ChildHome.CreateOrderChild -> {
                CreateOrderContent(screen.component)
            }
        }
    }
}

fun createHomeChild(
    config: HomeConfig,
    componentContext: ComponentContext,
    homeNavigation: StackNavigation<HomeConfig>,
    goToMessenger: () -> Unit,
    goToLogin: () -> Unit,
    navigateToMyOrders: () -> Unit,
    navigateToDialog: (dialogId: Long?) -> Unit,
    navigateToContactUs: () -> Unit,
): ChildHome = when (config) {
    HomeConfig.HomeScreen -> ChildHome.HomeChild(
        itemHome(
            componentContext,
            homeNavigation,
            goToLogin = { goToLogin() },
            navigateToMessenger = { goToMessenger() },
            navigateToContactUs = { navigateToContactUs() }
        )
    )

    is HomeConfig.OfferScreen -> ChildHome.OfferChild(
        component = offerFactory(
            componentContext,
            config.id,
            selectOffer = {
                val offerConfig = HomeConfig.OfferScreen(it, getCurrentDate())
                homeNavigation.pushNew(offerConfig)
            },
            onBack = {
                homeNavigation.pop()
            },
            onListingSelected = {
                homeNavigation.pushNew(
                    HomeConfig.ListingScreen(false, it.data.value, it.searchData.value)
                )
            },
            onUserSelected = { ui, about ->
                homeNavigation.pushNew(
                    HomeConfig.UserScreen(ui, getCurrentDate(), about)
                )
            },
            isSnapshot = config.isSnapshot,
            navigateToCreateOffer = { type, catPath, offerId, externalImages ->
                if (UserData.token != "") {
                    homeNavigation.pushNew(
                        HomeConfig.CreateOfferScreen(
                            catPath,
                            offerId,
                            type,
                            externalImages
                        )
                    )
                } else {
                    goToLogin()
                }
            },
            navigateToCreateOrder = {
                homeNavigation.pushNew(
                    HomeConfig.CreateOrderScreen(it)
                )
            },
            navigateToLogin = {
                goToLogin()
            },
            navigateToDialog = { dialogId ->
               navigateToDialog(dialogId)
            }
        )
    )
    is HomeConfig.ListingScreen -> {
        val ld = ListingData(
            searchData = MutableValue(config.searchData),
            data = MutableValue(config.listingData)
        )
        ChildHome.ListingChild(
            component = listingFactory(
                componentContext,
                ld,
                selectOffer = {
                    homeNavigation.pushNew(
                        HomeConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                onBack = {
                    homeNavigation.pop()
                },
                isOpenCategory = false,
                isOpenSearch = config.isOpenSearch
            ),
        )
    }

    is HomeConfig.UserScreen -> ChildHome.UserChild(
        component = userFactory(
            componentContext,
            config.userId,
            config.aboutMe,
            goToLogin = {
                homeNavigation.pushNew(
                    HomeConfig.ListingScreen(false, it.data.value, it.searchData.value)
                )
            },
            goBack = {
                homeNavigation.pop()
            },
            goToSnapshot = { id ->
                homeNavigation.pushNew(
                    HomeConfig.OfferScreen(id, getCurrentDate(), true)
                )
            },
            goToUser = {
                homeNavigation.pushNew(
                    HomeConfig.UserScreen(it, getCurrentDate(), false)
                )
            }
        )
    )
    is HomeConfig.CreateOfferScreen -> ChildHome.CreateOfferChild(
        component = createOfferFactory(
            componentContext = componentContext,
            catPath = config.catPath,
            offerId = config.offerId,
            type = config.createOfferType,
            externalImages = config.externalImages,
            navigateOffer = { id ->
                homeNavigation.pushNew(
                    HomeConfig.OfferScreen(id, getCurrentDate())
                )
            },
            navigateCreateOffer = { id, path, t ->
                homeNavigation.replaceCurrent(
                    HomeConfig.CreateOfferScreen(
                        catPath = path,
                        offerId = id,
                        createOfferType = t,
                    )
                )
            },
            navigateBack = {
                homeNavigation.pop()
            }
        )
    )

    is HomeConfig.CreateOrderScreen -> ChildHome.CreateOrderChild(
        component = createOrderFactory(
            componentContext = componentContext,
            selectedItems = config.basketItem,
            navigateUser = {
                homeNavigation.pushNew(
                    HomeConfig.UserScreen(it, getCurrentDate(), false)
                )
            },
            navigateOffer = {
                homeNavigation.pushNew(
                    HomeConfig.OfferScreen(it, getCurrentDate())
                )
            },
            navigateBack = {
                homeNavigation.pop()
            },
            navigateToMyOrders = {
                navigateToMyOrders()
            }
        )
    )
}

fun itemHome(
    componentContext: ComponentContext,
    homeNavigation : StackNavigation<HomeConfig>,
    goToLogin: () -> Unit,
    navigateToMessenger: () -> Unit,
    navigateToContactUs: () -> Unit
): HomeComponent {
    return DefaultHomeComponent(
        componentContext = componentContext,
        navigation = homeNavigation,
        navigateToListingSelected = { ld, isNewSearch ->
            homeNavigation.pushNew(
                HomeConfig.ListingScreen(
                    isNewSearch,
                    ld.data.value,
                    ld.searchData.value
                )
            )
        },
        navigateToLoginSelected = {
            goToLogin()
        },
        navigateToOfferSelected = { id ->
            homeNavigation.pushNew(HomeConfig.OfferScreen(id, getCurrentDate()))
        },
        navigateToCreateOfferSelected = {
            if(UserData.token != "") {
                homeNavigation.pushNew(
                    HomeConfig.CreateOfferScreen(
                        null,
                        null,
                        CreateOfferType.CREATE,
                        null
                    )
                )
            }else{
                goToLogin()
            }
        },
        navigateToMessengerSelected = {
            navigateToMessenger()
        },
        navigateToContactUsSelected = {
            navigateToContactUs()
        }
    )
}
