package market.engine.fragments.root.main.favPages

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import market.engine.core.data.types.FavScreenType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.favPages.favorites.FavoritesComponent
import market.engine.fragments.root.main.favPages.notes.NotesComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsComponent


interface FavPagesComponent {
    val componentsPages: Value<ChildPages<*, FavPagesComponents>>

    val favScreenType : FavScreenType

    fun selectPage(screenType: FavScreenType)

    fun onRefresh()
}

class DefaultFavPagesComponent(
    private val favoritesNavigation : StackNavigation<FavoritesConfig>,
    favType: FavScreenType,
    componentContext: ComponentContext,
) : FavPagesComponent, ComponentContext by componentContext {

    private val navigation = PagesNavigation<FavPagesConfig>()

    override val favScreenType: FavScreenType = favType

    override fun selectPage(screenType: FavScreenType) {
        when (screenType) {
            FavScreenType.FAVORITES -> {
                navigation.select(0)
            }

            FavScreenType.SUBSCRIBED -> {
                navigation.select(1)
            }

            FavScreenType.NOTES -> {
                navigation.select(2)
            }
        }
    }

    override fun onRefresh() {
        val index = componentsPages.value.selectedIndex
        when(val item = componentsPages.value.items[index].instance){
            is FavPagesComponents.FavoritesChild -> {
                item.component.onRefresh()
            }
            is FavPagesComponents.SubscribedChild -> {
                item.component.model.value.subViewModel.refresh()
            }
            is FavPagesComponents.NotesChild -> {
                item.component.onRefresh()
            }
            null -> {}
        }
    }

    override val componentsPages: Value<ChildPages<*, FavPagesComponents>> by lazy {
        childPages(
            source = navigation,
            serializer = FavPagesConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    listOf(
                        FavPagesConfig(FavScreenType.FAVORITES),
                        FavPagesConfig(FavScreenType.SUBSCRIBED),
                        FavPagesConfig(FavScreenType.NOTES)
                    ),
                    selectedIndex = if(favType == FavScreenType.FAVORITES) 0 else 1,
                )
            },
            key = "FavoritesStack",
            childFactory = { config, componentContext ->
                when (config.favType) {
                    FavScreenType.FAVORITES -> {
                        FavPagesComponents.FavoritesChild(
                            component = itemFavorites(
                                componentContext,
                                selectedFavScreen = {
                                    selectPage(it)
                                },
                                navigateToOffer = {
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.OfferScreen(
                                            it, getCurrentDate()
                                        )
                                    )
                                },
                                selectedType = config.favType
                            )
                        )
                    }
                    FavScreenType.SUBSCRIBED -> {
                        FavPagesComponents.SubscribedChild(
                            component = itemSubscriptions(
                                componentContext,
                                selectedFavScreen = {
                                    selectPage(it)
                                },
                                selectedType = config.favType,
                                navigateToCreateNewSubscription = {
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.CreateSubscriptionScreen(it)
                                    )
                                },
                                navigateToListing = {
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.ListingScreen(it.data.value, it.searchData.value, getCurrentDate())
                                    )
                                }
                            )
                        )
                    }

                    FavScreenType.NOTES -> {
                        FavPagesComponents.NotesChild(
                            component = itemNotes(
                                componentContext,
                                selectedFavScreen = {
                                    selectPage(it)
                                },
                                navigateToOffer = {
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.OfferScreen(
                                            it, getCurrentDate()
                                        )
                                    )
                                },
                                selectedType = config.favType
                            )
                        )
                    }
                }
            }
        )
    }
}

sealed class FavPagesComponents {
    class FavoritesChild(val component: FavoritesComponent) : FavPagesComponents()
    class SubscribedChild(val component: SubscriptionsComponent) : FavPagesComponents()
    class NotesChild(val component: NotesComponent) : FavPagesComponents()
}
