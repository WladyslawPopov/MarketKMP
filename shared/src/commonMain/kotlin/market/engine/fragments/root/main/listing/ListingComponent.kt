package market.engine.fragments.root.main.listing

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.SearchPagesType
import market.engine.core.utils.printLogD
import market.engine.fragments.root.main.favPages.itemSubscriptions
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsComponent

interface ListingComponent {
    val model : Value<Model>
    data class Model(
        var pagingDataFlow : Flow<PagingData<OfferItem>>,
        val listingViewModel: ListingViewModel,
        val backHandler: BackHandler
    )

    val searchPages: Value<ChildPages<*, SearchPagesComponents>>

    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun goBack()
    fun goToSubscribe()
    fun onTabSelect(tab: Int)
}

class DefaultListingComponent(
    isOpenCategory : Boolean,
    isOpenSearch : Boolean,
    componentContext: ComponentContext,
    listingData: ListingData,
    private val selectOffer: (Long) -> Unit,
    private val selectedBack: () -> Unit,
    private val navigateToSubscribe: () -> Unit,
    private val navigateToListing: (ListingData) -> Unit,
    private val navigateToNewSubscription: (Long?) -> Unit,
) : ListingComponent, ComponentContext by componentContext {

    private val listingViewModel : ListingViewModel = ListingViewModel()

    private val _model = MutableValue(
        ListingComponent.Model(
            pagingDataFlow = listingViewModel.init(listingData),
            listingViewModel = listingViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<ListingComponent.Model> = _model

    private val searchData = listingData.searchData.value
    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    private val navigator = PagesNavigation<SearchPagesConfig>()

    override val searchPages: Value<ChildPages<*, SearchPagesComponents>> = childPages(
        source = navigator,
        serializer = SearchPagesConfig.serializer(),
        initialPages = {
            Pages(
                buildList {
                    add(SearchPagesConfig(SearchPagesType.SEARCH_HISTORY))
                    if (UserData.token != "") {
                        add(SearchPagesConfig(SearchPagesType.SUBSCRIBED))
                    }
                },
                selectedIndex = 0
            )
        },
        handleBackButton = true,
        key = "SearchPagesComponents",
        childFactory = { config, componentContext ->
            when (config.type){
                SearchPagesType.SEARCH_HISTORY -> {
                    SearchPagesComponents.HistoryChild
                }
                SearchPagesType.SUBSCRIBED -> {
                    SearchPagesComponents.SubscriptionsChild(
                        component = itemSubscriptions(
                            componentContext,
                            selectedType = FavScreenType.SUBSCRIBED,
                            navigateToCreateNewSubscription = {
                                navigateToNewSubscription(it)
                            },
                            navigateToListing = { listingData ->
                                navigateToListing(listingData)
                            }
                        )
                    )
                }
            }
        }
    )

    init {
        val eventParameters = mapOf(
            "catalog_category" to searchData.searchCategoryName,
            "category_id" to searchData.searchCategoryID.toString()
        )
        analyticsHelper.reportEvent("open_catalog_listing", eventParameters)

        if(isOpenCategory)
            listingViewModel.activeFiltersType.value = "categories"

        listingViewModel.openSearch.value = isOpenSearch
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {

        if (isTopPromo){
            val eventParameters = mapOf(
                "lot_category" to offer.catPath.lastOrNull(),
                "lot_id" to offer.id,
            )

            analyticsHelper.reportEvent(
                "click_super_top_lots",
                eventParameters
            )
        }

        if (searchData.userSearch || searchData.searchString.isNotEmpty()){
            val eventParameters = mapOf(
                "lot_id" to offer.id,
                "lot_name" to offer.title,
                "lot_city" to offer.location,
                "auc_delivery" to offer.safeDeal,
                "lot_category" to offer.catPath.lastOrNull(),
                "seller_id" to offer.seller.id,
                "lot_price_start" to offer.price
            )
            analyticsHelper.reportEvent(
                "click_search_results_item",
                eventParameters
            )
        }else{
            val eventParameters = mapOf(
                "lot_id" to offer.id,
                "lot_name" to offer.title,
                "lot_city" to offer.location,
                "auc_delivery" to offer.safeDeal,
                "lot_category" to offer.catPath.lastOrNull(),
                "seller_id" to offer.seller.id,
                "lot_price_start" to offer.price
            )
            analyticsHelper.reportEvent(
                "click_item_at_catalog",
                eventParameters
            )
        }
        selectOffer(offer.id)

        lifecycle.doOnResume {
            listingViewModel.updateItem.value = offer.id
            printLogD("Update item Listing", offer.id.toString())
        }
    }

    override fun goBack() {
        selectedBack()
    }

    override fun goToSubscribe() {
        navigateToSubscribe()
    }

    override fun onTabSelect(tab: Int) {
        navigator.select(tab)
    }
}

@Serializable
data class SearchPagesConfig(
    @Serializable
    val type: SearchPagesType
)

sealed class SearchPagesComponents {
    class SubscriptionsChild(val component: SubscriptionsComponent) : SearchPagesComponents()
    data object HistoryChild : SearchPagesComponents()
}
