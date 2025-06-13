package market.engine.fragments.root.main.favPages.subscriptions

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.networkObjects.Subscription
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin


interface SubscriptionsComponent {
    val model : Value<Model>
    data class Model(
        val favType: FavScreenType,
        val pagingDataFlow : Flow<PagingData<Subscription>>,
        val subViewModel: SubViewModel,
        val backHandler: BackHandler
    )

    fun goToCreateNewSubscription(editId : Long? = null)

    fun goToListing(item : Subscription)
    fun updateItem(oldItem: Subscription?)
}

class DefaultSubscriptionsComponent(
    componentContext: ComponentContext,
    favType : FavScreenType,

    val navigateToCreateNewSubscription : (Long?) -> Unit,
    val navigateToListing : (ListingData) -> Unit,
) : SubscriptionsComponent, ComponentContext by componentContext {

    private val subViewModel : SubViewModel = SubViewModel(
        getKoin().get(),
    )

    private val _model = MutableValue(
        SubscriptionsComponent.Model(
            favType = favType,
            pagingDataFlow = subViewModel.init(),
            subViewModel = subViewModel,
            backHandler = backHandler
        )
    )

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            subViewModel.updateUserInfo()
        }

        analyticsHelper.reportEvent("open_subscribes", mapOf())
    }

    override val model: Value<SubscriptionsComponent.Model> = _model

    override fun goToCreateNewSubscription(editId: Long?) {
        navigateToCreateNewSubscription(editId)
        lifecycle.doOnResume {
            subViewModel.updateItem.value = editId
        }
    }

    override fun goToListing(subscription: Subscription) {
        subViewModel.viewModelScope.launch {
            val price = getString(strings.priceParameterName)
            val from = getString(strings.fromAboutParameterName)
            val to = getString(strings.toAboutParameterName)
            val currency = getString(strings.currencyCode)
            val defCat = getString(strings.categoryMain)

            val ld = ListingData()
            ld.data.filters = ListingFilters.getEmpty()

            if (subscription.priceTo != null) {
                ld.data.filters.find {
                    it.key == "current_price" && it.operation == "lte"
                }?.let {
                    it.value = subscription.priceTo ?: ""
                    it.interpretation =
                        "$price $from - ${subscription.priceTo} $currency"
                }
            }

            if (subscription.priceFrom != null) {
                ld.data.filters.find {
                    it.key == "current_price" && it.operation == "gte"
                }?.let {
                    it.value = subscription.priceFrom ?: ""
                    it.interpretation =
                        "$price $to - ${subscription.priceFrom} $currency"
                }
            }

            if (subscription.region != null) {
                ld.data.filters.find {
                    it.key == "region"
                }?.let {
                    it.value = (subscription.region?.code ?: "").toString()
                    it.interpretation = subscription.region?.name ?: ""
                }
            }

            if (subscription.saleType != null) {
                ld.data.filters.find {
                    it.key == "sale_type"
                }?.let {
                    when (subscription.saleType) {
                        "buy_now" -> {
                            it.value = "buynow"
                            it.interpretation = ""
                        }

                        "ordinary_auction" -> {
                            it.value = "auction"
                            it.interpretation = ""
                        }
                    }
                }
            }

            if (subscription.sellerData != null) {
                ld.searchData.userSearch = true
                ld.searchData.userID = subscription.sellerData.id
                ld.searchData.userLogin = subscription.sellerData.login
            }

            ld.searchData.searchString = subscription.searchQuery ?: ""
            ld.searchData.searchCategoryID =
                subscription.catpath?.keys?.firstOrNull() ?: 1L
            ld.searchData.searchCategoryName =
                subscription.catpath?.values?.firstOrNull() ?: defCat

            navigateToListing(ld)
        }
    }

    override fun updateItem(oldItem: Subscription?) {
        subViewModel.viewModelScope.launch {
            subViewModel.getSubscription(subViewModel.updateItem.value!!){ item ->
                if (item != null) {
                    if (oldItem != null) {
                        oldItem.catpath = item.catpath
                        oldItem.isEnabled = item.isEnabled
                        oldItem.name = item.name
                        oldItem.priceFrom = item.priceFrom
                        oldItem.priceTo = item.priceTo
                        oldItem.region = item.region
                        oldItem.searchQuery = item.searchQuery
                        oldItem.saleType = item.saleType
                    }
                } else {
                    oldItem?.id = 1L
                }
                subViewModel.updateItemTrigger.value++
                subViewModel.updateItem.value = null
            }
        }
    }
}
