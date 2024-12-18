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
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.ListingData
import market.engine.navigation.main.publicItems.itemCreateOffer
import market.engine.navigation.main.publicItems.itemListing
import market.engine.navigation.main.publicItems.itemOffer
import market.engine.navigation.main.publicItems.itemUser
import market.engine.core.data.types.CreateOfferType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.CreateOfferContent
import market.engine.fragments.listing.ListingComponent
import market.engine.fragments.offer.OfferContent
import market.engine.fragments.listing.ListingContent
import market.engine.fragments.offer.OfferComponent
import market.engine.fragments.user.UserComponent
import market.engine.fragments.user.UserContent

@Serializable
sealed class SearchConfig {
    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD, val isOpenSearch : Boolean) : SearchConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : SearchConfig()

    @Serializable
    data class UserScreen(val id: Long, val ts: String, val aboutMe : Boolean) : SearchConfig()

    @Serializable
    data class CreateOfferScreen(
        val categoryId: Long,
        val offerId: Long? = null,
        val type : CreateOfferType,
        val externalImages : List<String>? = null
    ) : SearchConfig()
}

sealed class ChildSearch {
    class ListingChild(val component: ListingComponent) : ChildSearch()
    class OfferChild(val component: OfferComponent) : ChildSearch()
    class UserChild(val component: UserComponent) : ChildSearch()
    class CreateOfferChild(val component: CreateOfferComponent) : ChildSearch()
}

@Composable
fun SearchNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildSearch>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildSearch.ListingChild -> ListingContent(screen.component, modifier)
            is ChildSearch.OfferChild -> OfferContent(screen.component, modifier)
            is ChildSearch.UserChild -> UserContent(screen.component, modifier)
            is ChildSearch.CreateOfferChild -> CreateOfferContent(screen.component)
        }
    }
}

fun createSearchChild(
    config: SearchConfig,
    componentContext: ComponentContext,
    searchNavigation: StackNavigation<SearchConfig>,
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
                        searchNavigation.pushNew(
                            SearchConfig.OfferScreen(
                                it,
                                getCurrentDate()
                            )
                        )
                    },
                    onBack = {
                        searchNavigation.pop()
                    },
                    isOpenCategory = false,
                    isOpenSearch = config.isOpenSearch
                ),
            )
        }
        is SearchConfig.OfferScreen -> ChildSearch.OfferChild(
            component = itemOffer(
                componentContext,
                config.id,
                selectOffer = {
                    searchNavigation.pushNew(
                        SearchConfig.OfferScreen(
                            it,
                            getCurrentDate(),
                        )
                    )
                },
                onBack = {
                    searchNavigation.pop()
                },
                onListingSelected = {
                    searchNavigation.pushNew(
                        SearchConfig.ListingScreen(it.data.value, it.searchData.value, false)
                    )
                },
                onUserSelected = { ui, about ->
                    searchNavigation.pushNew(
                        SearchConfig.UserScreen(ui, getCurrentDate(), about)
                    )
                },
                config.isSnapshot,
                navigateToCreateOffer = { type, offerId, externalImages ->
                    searchNavigation.pushNew(
                        SearchConfig.CreateOfferScreen(
                            categoryId = 1L,
                            type = type,
                            externalImages = externalImages,
                            offerId = offerId
                        )
                    )
                }
            )
        )
        is SearchConfig.UserScreen -> ChildSearch.UserChild(
            component = itemUser(
                componentContext,
                config.id,
                config.aboutMe,
                goToLogin = {
                    searchNavigation.pushNew(
                        SearchConfig.ListingScreen(it.data.value, it.searchData.value, false)
                    )
                },
                goBack = {
                    searchNavigation.pop()
                },
                goToSnapshot = { id ->
                    searchNavigation.pushNew(
                        SearchConfig.OfferScreen(id, getCurrentDate(), true)
                    )
                },
                goToUser = {
                    searchNavigation.pushNew(
                        SearchConfig.UserScreen(it, getCurrentDate(), false)
                    )
                }
            )
        )
        is SearchConfig.CreateOfferScreen -> ChildSearch.CreateOfferChild(
            component = itemCreateOffer(
                componentContext = componentContext,
                categoryId = config.categoryId,
                offerId = config.offerId,
                type = config.type,
                externalImages = config.externalImages,
                navigateBack = {
                    searchNavigation.pop()
                }
            )
        )
    }
