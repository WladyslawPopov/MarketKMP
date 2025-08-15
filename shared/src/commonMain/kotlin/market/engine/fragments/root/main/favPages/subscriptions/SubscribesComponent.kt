package market.engine.fragments.root.main.favPages.subscriptions

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.networkObjects.Subscription
import market.engine.fragments.base.listing.ListingBaseViewModel
import org.jetbrains.compose.resources.getString


interface SubscriptionsComponent {

    val additionalModels : Value<AdditionalModels>
    data class AdditionalModels(
        val listingBaseViewModel: ListingBaseViewModel
    )

    val model : Value<Model>
    data class Model(
        val favType: FavScreenType,
        val subViewModel: SubViewModel,
        val backHandler: BackHandler
    )

    fun goToCreateNewSubscription(editId : Long? = null)
    fun goToListing(item : Subscription)
    fun goToOffer(id : Long)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultSubscriptionsComponent(
    componentContext: JetpackComponentContext,
    favType : FavScreenType,
    val navigateToCreateNewSubscription : (Long?) -> Unit,
    val navigateToListing : (ListingData) -> Unit,
    val navigateToOffer : (Long) -> Unit,
) : SubscriptionsComponent, JetpackComponentContext by componentContext {

    val listingBaseVM = viewModel("subBaseViewModel"){
        ListingBaseViewModel(
            savedStateHandle = createSavedStateHandle()
        )
    }

    private val _additionalModels = MutableValue(
        SubscriptionsComponent.AdditionalModels(
            listingBaseVM
        )
    )

    override val additionalModels: Value<SubscriptionsComponent.AdditionalModels> = _additionalModels


    private val subViewModel = viewModel("subViewModel"){
        SubViewModel(this@DefaultSubscriptionsComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        SubscriptionsComponent.Model(
            favType = favType,
            subViewModel = subViewModel,
            backHandler = backHandler
        )
    )

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    private val updateBackHandlerItem = MutableValue(1L)

    init {
        lifecycle.doOnResume {
            subViewModel.updateUserInfo()

            if (updateBackHandlerItem.value != 1L) {
                subViewModel.setUpdateItem(updateBackHandlerItem.value)
                updateBackHandlerItem.value = 1L
            }
        }

        analyticsHelper.reportEvent("open_subscribes", mapOf())
    }

    override val model: Value<SubscriptionsComponent.Model> = _model

    override fun goToCreateNewSubscription(editId: Long?) {
        updateBackHandlerItem.value = editId ?: 1L
        navigateToCreateNewSubscription(editId)
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
            withContext(Dispatchers.Main){
                navigateToListing(ld)
            }
        }
    }

    override fun goToOffer(id: Long) {
        navigateToOffer(id)
    }
}
