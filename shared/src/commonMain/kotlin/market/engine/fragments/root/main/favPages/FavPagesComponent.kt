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
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.ServerErrorException
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

    fun fullRefresh()

    fun onRefresh()
}

class DefaultFavPagesComponent(
    private val favoritesNavigation : StackNavigation<FavoritesConfig>,
    val viewModel: FavPagesViewModel,
    val favType: FavScreenType,
    componentContext: ComponentContext,
) : FavPagesComponent, ComponentContext by componentContext {

    private val navigation = PagesNavigation<FavPagesConfig>()

    override val favScreenType: FavScreenType = favType

    val favTabs = viewModel.favoritesTabList
    val initPos = viewModel.initPosition


    private var initialModel = MutableValue(
        FavPagesComponent.Model(
            viewModel = viewModel,
        )
    )

    override val componentsPages: Value<ChildPages<*, FavPagesComponents>> =
        childPages(
            source = navigation,
            serializer = FavPagesConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                val list = favTabs.value.map {
                    FavPagesConfig(it)
                }

                Pages(
                    list,
                    selectedIndex =
                        when{
                            favType == FavScreenType.SUBSCRIBED -> {
                                list.indexOf(list.find { it.favItem.id == 222L })
                            }
                            else -> {
                                (initPos.value).coerceIn(0, (initPos.value-1).coerceAtLeast(0))
                            }
                        },
                )
            },
            key = "FavoritesStack",
            childFactory = { config, componentContext ->
                when (config.favItem.id) {
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
                                        FavoritesConfig.ListingScreen(it.data, it.searchData, getCurrentDate())
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
                                favType =  when(config.favItem.id){
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

    override val model = initialModel

    override fun selectPage(p: Int) {
        viewModel.selectPage(p)
        navigation.select(p)
    }

    override fun fullRefresh() {
        viewModel.getFavTabList {
            favoritesNavigation.replaceCurrent(
                FavoritesConfig.FavPagesScreen(
                    favType,
                    getCurrentDate()
                )
            )
        }
    }

    override fun onRefresh() {
        viewModel.onError(ServerErrorException())
        val index = componentsPages.value.selectedIndex
        when(val item = componentsPages.value.items[index].instance){
            is FavPagesComponents.FavoritesChild -> {
                item.component.onRefresh()
            }
            is FavPagesComponents.SubscribedChild -> {
                item.component.onRefresh()
            }
            null -> {}
        }
    }
}

sealed class FavPagesComponents {
    class FavoritesChild(val component: FavoritesComponent) : FavPagesComponents()
    class SubscribedChild(val component: SubscriptionsComponent) : FavPagesComponents()
}
