package market.engine.fragments.root.main.favPages

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.data.items.Tab
import market.engine.core.data.types.FavScreenType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.favPages.favorites.DefaultFavoritesComponent
import market.engine.fragments.root.main.favPages.favorites.FavoritesComponent
import market.engine.fragments.root.main.favPages.subscriptions.DefaultSubscriptionsComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsComponent


interface FavPagesComponent {
    val componentsPages: Value<ChildPages<*, FavPagesComponents>>

    val favScreenType : FavScreenType

    val model : Value<Model>
    data class Model(
        val viewModel: FavPagesViewModel
    )

    fun selectPage(p: Int)

    fun updateNavigationPages()

    fun onRefresh()
}

class DefaultFavPagesComponent(
    private val favoritesNavigation : StackNavigation<FavoritesConfig>,
    val favType: FavScreenType,
    componentContext: ComponentContext,
) : FavPagesComponent, ComponentContext by componentContext {

    val viewModel = FavPagesViewModel(this)

    private val navigation = PagesNavigation<FavPagesConfig>()

    override val favScreenType: FavScreenType = favType

    private var initialModel = MutableValue(
        FavPagesComponent.Model(
            viewModel = viewModel
        )
    )

    override val model = initialModel

    override fun updateNavigationPages(){
        val tabs = viewModel.favoritesTabList.value
        val initPosition = viewModel.initPosition.value

        val configs = tabs.map { FavPagesConfig(it.id) }

        val selectedIndex = calculateSelectedIndex(tabs, favType, initPosition)

        navigation.navigate(
            transformer = {
                Pages(
                    items = configs,
                    selectedIndex = selectedIndex
                )
            },
            onComplete = { _, _ -> }
        )

        viewModel.analyticsHelper.reportEvent(
            "update_offers_list", mapOf()
        )
    }

    private fun calculateSelectedIndex(tabs: List<Tab>, favType: FavScreenType, initPosition: Int): Int {
        return when {
            favType == FavScreenType.SUBSCRIBED -> {
                tabs.indexOfFirst { it.id == 222L }.coerceAtLeast(0)
            }
            else -> {
                initPosition.coerceIn(0, (tabs.lastIndex).coerceAtLeast(0))
            }
        }
    }

    override val componentsPages: Value<ChildPages<*, FavPagesComponents>> by lazy {
        childPages(
            source = navigation,
            serializer = FavPagesConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                val tabs = viewModel.favoritesTabList.value
                val initPosition = viewModel.initPosition.value

                Pages(
                    tabs.map {
                        FavPagesConfig(it.id)
                    },
                    selectedIndex = calculateSelectedIndex(tabs, favType, initPosition),
                )
            },
            key = "FavoritesStack",
            childFactory = { config, componentContext ->
                when (config.favItemId) {
                    222L -> {
                        FavPagesComponents.SubscribedChild(
                            component = DefaultSubscriptionsComponent(
                                componentContext = componentContext,
                                favType = FavScreenType.SUBSCRIBED,
                                navigateToCreateNewSubscription = {
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.CreateSubscriptionScreen(it)
                                    )
                                },
                                navigateToListing = {
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.ListingScreen(
                                            it.data,
                                            it.searchData,
                                            getCurrentDate()
                                        )
                                    )
                                }
                            )
                        )
                    }

                    else -> {
                        FavPagesComponents.FavoritesChild(
                            component = DefaultFavoritesComponent(
                                componentContext = componentContext,
                                goToOffer = { id ->
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.OfferScreen(
                                            id, getCurrentDate()
                                        )
                                    )
                                },
                                favType = when (config.favItemId) {
                                    111L -> {
                                        FavScreenType.FAVORITES
                                    }

                                    333L -> {
                                        FavScreenType.NOTES
                                    }

                                    else -> {
                                        FavScreenType.FAV_LIST
                                    }
                                },
                                idList = config.favItemId,
                                updateTabs = {
                                    viewModel.getFavTabList()
                                },
                                navigateToProposalPage = { type, id ->
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.ProposalScreen(
                                            id, type, getCurrentDate()
                                        )
                                    )
                                },
                                navigateToCreateOffer = { type, id ->
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.CreateOfferScreen(null, id, type)
                                    )
                                }
                            )
                        )
                    }
                }
            }
        )
    }

    override fun selectPage(p: Int) {
        navigation.select(p)
    }

    override fun onRefresh() {
        viewModel.refresh()
        val index = componentsPages.value.selectedIndex
        when(val item = componentsPages.value.items[index].instance){
            is FavPagesComponents.FavoritesChild -> {
                item.component.model.value.favViewModel.refresh()
            }
            is FavPagesComponents.SubscribedChild -> {
                item.component.model.value.subViewModel.refresh()
            }
            null -> {}
        }
    }
}

sealed class FavPagesComponents {
    class FavoritesChild(val component: FavoritesComponent) : FavPagesComponents()
    class SubscribedChild(val component: SubscriptionsComponent) : FavPagesComponents()
}
