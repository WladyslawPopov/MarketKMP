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
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer


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
    fun onRefresh()
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    favType : FavScreenType,
    val selectedFavScreen : (FavScreenType) -> Unit,
    val goToOffer : (Long) -> Unit
) : FavoritesComponent, ComponentContext by componentContext {

    private val favViewModel : FavViewModel = FavViewModel()

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

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            favViewModel.updateUserInfo()
        }

        analyticsHelper.reportEvent("open_favorites", mapOf())
    }

    private val searchData = listingData.searchData
    //private val listingData = model.value.listingData.data

    override fun goToOffer(offer: Offer, isTopPromo : Boolean) {
        goToOffer(offer.id)
        lifecycle.doOnResume {
            favViewModel.updateItem.value = offer.id
        }
    }

    override fun onRefresh() {
        favViewModel.onError(ServerErrorException())
        favViewModel.resetScroll()
        favViewModel.refresh()
    }
}
