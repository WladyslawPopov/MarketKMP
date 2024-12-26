package market.engine.navigation.main.children

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
import market.engine.navigation.main.publicItems.itemCreateOffer
import market.engine.navigation.main.publicItems.itemListing
import market.engine.navigation.main.publicItems.itemOffer
import market.engine.navigation.main.publicItems.itemUser
import market.engine.core.data.types.CreateOfferType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.home.DefaultHomeComponent
import market.engine.fragments.home.HomeComponent
import market.engine.fragments.home.HomeContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent

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
}

sealed class ChildHome {
    class HomeChild(val component: HomeComponent) : ChildHome()
    class OfferChild(val component: OfferComponent) : ChildHome()
    class ListingChild(val component: ListingComponent) : ChildHome()
    class UserChild(val component: UserComponent) : ChildHome()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildHome()
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
        }
    }
}

fun createHomeChild(
    config: HomeConfig,
    componentContext: ComponentContext,
    homeNavigation: StackNavigation<HomeConfig>,
    goToLogin: () -> Unit
): ChildHome = when (config) {
    HomeConfig.HomeScreen -> ChildHome.HomeChild(
        itemHome(
            componentContext,
            homeNavigation,
            goToLogin = { goToLogin() }
        )
    )

    is HomeConfig.OfferScreen -> ChildHome.OfferChild(
        component = itemOffer(
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
                if(UserData.token != "") {
                    homeNavigation.pushNew(
                        HomeConfig.CreateOfferScreen(
                            catPath,
                            offerId,
                            type,
                            externalImages
                        )
                    )
                }else{
                    goToLogin()
                }
            }
        )
    )
    is HomeConfig.ListingScreen -> {
        val ld = ListingData(
            searchData = MutableValue(config.searchData),
            data = MutableValue(config.listingData)
        )
        ChildHome.ListingChild(
            component = itemListing(
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
        component = itemUser(
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
        component = itemCreateOffer(
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
}

fun itemHome(
    componentContext: ComponentContext,
    homeNavigation : StackNavigation<HomeConfig>,
    goToLogin: () -> Unit
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
        }
    )
}
