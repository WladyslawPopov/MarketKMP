package market.engine.fragments.root.main.favPages.subscriptions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.launch
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.networkObjects.Subscription
import org.jetbrains.compose.resources.getString


interface SubscriptionsComponent {
    val model : Value<Model>
    data class Model(
        val favType: FavScreenType,
        val subViewModel: SubViewModel,
        val backHandler: BackHandler
    )

    fun goToCreateNewSubscription(editId : Long? = null)
    fun goToListing(item : Subscription)
    fun onRefresh()
}

class DefaultSubscriptionsComponent(
    componentContext: ComponentContext,
    favType : FavScreenType,

    val navigateToCreateNewSubscription : (Long?) -> Unit,
    val navigateToListing : (ListingData) -> Unit,
) : SubscriptionsComponent, ComponentContext by componentContext {

    private val subViewModel : SubViewModel = SubViewModel(this)

    private val _model = MutableValue(
        SubscriptionsComponent.Model(
            favType = favType,
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
            subViewModel.setUpdateItem(editId)
        }
    }

    override fun goToListing(item: Subscription) {
        subViewModel.viewModelScope.launch {
            val price = getString(strings.priceParameterName)
            val from = getString(strings.fromAboutParameterName)
            val to = getString(strings.toAboutParameterName)
            val currency = getString(strings.currencyCode)
            val defCat = getString(strings.categoryMain)

            val ld = ListingData()
            ld.data.filters = ListingFilters.getEmpty()

            if (item.priceTo != null) {
                ld.data.filters.find {
                    it.key == "current_price" && it.operation == "lte"
                }?.let {
                    it.value = item.priceTo ?: ""
                    it.interpretation =
                        "$price $from - ${item.priceTo} $currency"
                }
            }

            if (item.priceFrom != null) {
                ld.data.filters.find {
                    it.key == "current_price" && it.operation == "gte"
                }?.let {
                    it.value = item.priceFrom ?: ""
                    it.interpretation =
                        "$price $to - ${item.priceFrom} $currency"
                }
            }

            if (item.region != null) {
                ld.data.filters.find {
                    it.key == "region"
                }?.let {
                    it.value = (item.region?.code ?: "").toString()
                    it.interpretation = item.region?.name ?: ""
                }
            }

            if (item.saleType != null) {
                ld.data.filters.find {
                    it.key == "sale_type"
                }?.let {
                    when (item.saleType) {
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

            if (item.sellerData != null) {
                ld.searchData.userSearch = true
                ld.searchData.userID = item.sellerData.id
                ld.searchData.userLogin = item.sellerData.login
            }

            ld.searchData.searchString = item.searchQuery ?: ""
            ld.searchData.searchCategoryID =
                item.catpath?.keys?.firstOrNull() ?: 1L
            ld.searchData.searchCategoryName =
                item.catpath?.values?.firstOrNull() ?: defCat

            navigateToListing(ld)
        }
    }

    override fun onRefresh() {
        subViewModel.updatePage()
    }
}
