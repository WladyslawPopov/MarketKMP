package market.engine.fragments.root.main.favPages.favorites


import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.networkObjects.Offer
import org.koin.mp.KoinPlatform.getKoin


interface FavoritesComponent {
    val model : Value<Model>
    data class Model(
        val favType: FavScreenType,
        val pagingDataFlow : Flow<PagingData<Offer>>,
        val favViewModel: FavViewModel,
        val backHandler: BackHandler
    )

    fun goToFavScreen()
    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    favType : FavScreenType,
    val selectedFavScreen : (FavScreenType) -> Unit,
    val goToOffer : (Long) -> Unit
) : FavoritesComponent, ComponentContext by componentContext {

    private val favViewModel : FavViewModel = getKoin().get()

    val listingData = favViewModel.listingData.value

    private val _model = MutableValue(
        FavoritesComponent.Model(
            favType = favType,
            favViewModel = favViewModel,
            pagingDataFlow = favViewModel.init(),
            backHandler = backHandler
        )
    )

    override val model: Value<FavoritesComponent.Model> = _model

    override fun goToFavScreen() {
        selectedFavScreen(model.value.favType)
    }

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            favViewModel.updateUserInfo()
        }

        analyticsHelper.reportEvent("open_favorites", mapOf())
    }

    private val searchData = listingData.searchData
    //private val listingData = model.value.listingData.data

    override fun goToOffer(offer: Offer, isTopPromo : Boolean) {
        if (isTopPromo){
            val eventParameters = mapOf(
                "lot_category" to offer.catpath.lastOrNull(),
                "lot_id" to offer.id,
            )

            analyticsHelper.reportEvent(
                "click_super_top_lots",
                eventParameters
            )
        }
        if (searchData.value.userSearch || searchData.value.searchString.isNotEmpty()){
            val eventParameters = mapOf(
                "lot_id" to offer.id,
                "lot_name" to offer.title,
                "lot_city" to offer.freeLocation,
                "auc_delivery" to offer.safeDeal,
                "lot_category" to offer.catpath.lastOrNull(),
                "seller_id" to offer.sellerData?.id,
                "lot_price_start" to offer.currentPricePerItem
            )
            analyticsHelper.reportEvent(
                "click_search_results_item",
                eventParameters
            )
        }else{
            val eventParameters = mapOf(
                "lot_id" to offer.id,
                "lot_name" to offer.title,
                "lot_city" to offer.freeLocation,
                "auc_delivery" to offer.safeDeal,
                "lot_category" to offer.catpath.lastOrNull(),
                "seller_id" to offer.sellerData?.id,
                "lot_price_start" to offer.currentPricePerItem
            )
            analyticsHelper.reportEvent(
                "click_item_at_catalog",
                eventParameters
            )
        }
        goToOffer(offer.id)
        lifecycle.doOnResume {
            favViewModel.updateItem.value = offer.id
        }
    }
}
