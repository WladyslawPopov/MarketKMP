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
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.CreateOfferType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.basket.BasketComponent
import market.engine.fragments.basket.BasketContent
import market.engine.fragments.basket.DefaultBasketComponent
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent
import market.engine.navigation.main.publicItems.itemCreateOffer
import market.engine.navigation.main.publicItems.itemListing
import market.engine.navigation.main.publicItems.itemOffer
import market.engine.navigation.main.publicItems.itemUser

@Serializable
sealed class BasketConfig {
    @Serializable
    data object BasketScreen : BasketConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : BasketConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnap: Boolean = false) : BasketConfig()
    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe : Boolean) : BasketConfig()

    @Serializable
    data class CreateOfferScreen(
        val catPath: List<Long>?,
        val offerId: Long? = null,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>? = null
    ) : BasketConfig()
}

sealed class ChildBasket {
    class BasketChild(val component: BasketComponent) : ChildBasket()
    class ListingChild(val component: ListingComponent) : ChildBasket()
    class OfferChild(val component: OfferComponent) : ChildBasket()
    class UserChild(val component: UserComponent) : ChildBasket()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildBasket()
}

@Composable
fun BasketNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildBasket>>
) {
    val stack by childStack.subscribeAsState()
    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildBasket.BasketChild -> BasketContent(screen.component)
            is ChildBasket.ListingChild -> ListingContent(screen.component, modifier)
            is ChildBasket.OfferChild -> OfferContent(screen.component, modifier)
            is ChildBasket.CreateOfferChild -> CreateOfferContent(screen.component)
            is ChildBasket.UserChild -> UserContent(screen.component, modifier)
        }
    }
}


fun createBasketChild(
    config: BasketConfig,
    componentContext: ComponentContext,
    basketNavigation : StackNavigation<BasketConfig>
): ChildBasket = 
    when (config) {
        BasketConfig.BasketScreen -> ChildBasket.BasketChild(
            itemBasket(componentContext, basketNavigation)
        )

        is BasketConfig.ListingScreen -> {
            val ld = ListingData(
                searchData = MutableValue(config.searchData),
                data = MutableValue(config.listingData)
            )
            ChildBasket.ListingChild(
                itemListing(
                    componentContext,
                    ld,
                    selectOffer = { 
                        basketNavigation.pushNew(BasketConfig.OfferScreen(it,getCurrentDate()))
                    },
                    onBack = {
                        basketNavigation.pop()
                    },
                    isOpenCategory = false
                )
            )
        }

        is BasketConfig.OfferScreen -> ChildBasket.OfferChild(
            component = itemOffer(
                componentContext,
                config.id,
                selectOffer = {
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(it, getCurrentDate())
                    )
                },
                onBack = {
                    basketNavigation.pop()
                },
                onListingSelected = {
                    basketNavigation.pushNew(
                        BasketConfig.ListingScreen(it.data.value, it.searchData.value)
                    )
                },
                onUserSelected = { ui, about ->
                    basketNavigation.pushNew(
                        BasketConfig.UserScreen(ui, getCurrentDate(), about)
                    )
                },
                isSnapshot = config.isSnap,
                navigateToCreateOffer = { type, catPath, offerId, externalImages ->
                    basketNavigation.pushNew(
                        BasketConfig.CreateOfferScreen(
                            catPath = catPath,
                            createOfferType = type,
                            externalImages = externalImages,
                            offerId = offerId
                        )
                    )
                }
            )
        )

        is BasketConfig.CreateOfferScreen -> ChildBasket.CreateOfferChild(
            component = itemCreateOffer(
                componentContext = componentContext,
                catPath = config.catPath,
                offerId = config.offerId,
                type = config.createOfferType,
                externalImages = config.externalImages,
                navigateOffer = { id ->
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(id, getCurrentDate())
                    )
                },
                navigateCreateOffer = { id, path, t ->
                    basketNavigation.replaceCurrent(
                        BasketConfig.CreateOfferScreen(
                            catPath = path,
                            offerId = id,
                            createOfferType = t,
                        )
                    )
                },
                navigateBack = {
                    basketNavigation.pop()
                }
            )
        )
        is BasketConfig.UserScreen -> ChildBasket.UserChild(
            itemUser(
                componentContext,
                config.userId,
                config.aboutMe,
                goToLogin = {
                    basketNavigation.pushNew(
                        BasketConfig.ListingScreen(it.data.value, it.searchData.value)
                    )
                },
                goBack = {
                    basketNavigation.pop()
                },
                goToSnapshot = { id ->
                    basketNavigation.pushNew(
                        BasketConfig.OfferScreen(id, getCurrentDate(), true)
                    )
                },
                goToUser = {
                    basketNavigation.pushNew(
                        BasketConfig.UserScreen(it, getCurrentDate(), false)
                    )
                }
            )
        )
    }

fun itemBasket(componentContext: ComponentContext, basketNavigation : StackNavigation<BasketConfig>): BasketComponent {
    return DefaultBasketComponent(
        componentContext = componentContext,
        navigateToListing = {
            basketNavigation.pushNew(BasketConfig.ListingScreen(LD(), SD()))
        }
    )
}
