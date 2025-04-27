package market.engine.fragments.root.main.favPages

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.favPages.favorites.FavoritesComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsComponent
import org.koin.mp.KoinPlatform.getKoin


interface FavPagesComponent {
    var componentsPages: Value<ChildPages<*, FavPagesComponents>>

    val favScreenType : FavScreenType

    val model : Value<Model>
    data class Model(
        val viewModel: FavPagesViewModel
    )

    fun selectPage(p: Int)

    fun getPages(version : String)

    fun fullRefresh()

    fun onRefresh()
}

class DefaultFavPagesComponent(
    private val favoritesNavigation : StackNavigation<FavoritesConfig>,
    val favType: FavScreenType,
    componentContext: ComponentContext,
) : FavPagesComponent, ComponentContext by componentContext {

    private val navigation = PagesNavigation<FavPagesConfig>()

    private val viewModel = FavPagesViewModel(getKoin().get())


    override val favScreenType: FavScreenType = favType
    private var initialModel = MutableValue(
        FavPagesComponent.Model(
            viewModel = viewModel,
        )
    )
    override val model = initialModel

    init {
        lifecycle.doOnResume {
            viewModel.getFavTabList{
                getPages(getCurrentDate())
            }
        }
    }

    override fun selectPage(p: Int) {
        navigation.select(p)
    }

    override fun getPages(version : String) {
        componentsPages = childPages(
            source = navigation,
            serializer = FavPagesConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    viewModel.favoritesTabList.value.map {
                        FavPagesConfig(it)
                    },
                    selectedIndex = if(favType == FavScreenType.FAVORITES) 0 else 1,
                )
            },
            key = "FavoritesStack_$version",
            childFactory = { config, componentContext ->
                when (config.favItem.id) {
                    222L -> {
                        FavPagesComponents.SubscribedChild(
                            component = itemSubscriptions(
                                componentContext,
                                selectedType = FavScreenType.SUBSCRIBED,
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
                    else -> {
                        FavPagesComponents.FavoritesChild(
                            component = itemFavorites(
                                componentContext,
                                navigateToOffer = {
                                    favoritesNavigation.pushNew(
                                        FavoritesConfig.OfferScreen(
                                            it, getCurrentDate()
                                        )
                                    )
                                },
                                selectedType =
                                    when(config.favItem.id){
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
                                idList = config.favItem.id,
                                updateTabs = {
                                    fullRefresh()
                                }
                            )
                        )
                    }
                }
            }
        )
    }

    override fun fullRefresh() {
        favoritesNavigation.replaceCurrent(FavoritesConfig.FavPagesScreen(favType, getCurrentDate()))
    }

    override fun onRefresh() {
        viewModel.onError(ServerErrorException())
        val index = componentsPages.value.selectedIndex
        when(val item = componentsPages.value.items[index].instance){
            is FavPagesComponents.FavoritesChild -> {
                item.component.onRefresh()
            }
            is FavPagesComponents.SubscribedChild -> {
                item.component.model.value.subViewModel.refresh()
            }
            null -> {}
        }
    }

    override var componentsPages: Value<ChildPages<*, FavPagesComponents>> = childPages(
        source = navigation,
        serializer = FavPagesConfig.serializer(),
        handleBackButton = true,
        initialPages = {
            Pages(
                viewModel.favoritesTabList.value.map {
                    FavPagesConfig(it)
                },
                selectedIndex = if(favType == FavScreenType.FAVORITES) 0 else 1,
            )
        },
        key = "FavoritesStack",
        childFactory = { config, componentContext ->
            when (config.favItem.id) {
                222L -> {
                    FavPagesComponents.SubscribedChild(
                        component = itemSubscriptions(
                            componentContext,
                            selectedType = FavScreenType.SUBSCRIBED,
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
                else -> {
                    FavPagesComponents.FavoritesChild(
                        component = itemFavorites(
                            componentContext,
                            navigateToOffer = {
                                favoritesNavigation.pushNew(
                                    FavoritesConfig.OfferScreen(
                                        it, getCurrentDate()
                                    )
                                )
                            },
                            selectedType =
                                when(config.favItem.id){
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
                            idList = config.favItem.id,
                            updateTabs = {
                                fullRefresh()
                            }
                        )
                    )
                }
            }
        }
    )
}

sealed class FavPagesComponents {
    class FavoritesChild(val component: FavoritesComponent) : FavPagesComponents()
    class SubscribedChild(val component: SubscriptionsComponent) : FavPagesComponents()
}
