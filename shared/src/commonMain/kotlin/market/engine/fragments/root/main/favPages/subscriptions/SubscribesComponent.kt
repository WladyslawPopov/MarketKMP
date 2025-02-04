package market.engine.fragments.root.main.favPages.subscriptions

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Subscription
import market.engine.fragments.root.main.favPages.favorites.FavViewModel
import org.koin.mp.KoinPlatform.getKoin


interface SubscriptionsComponent {
    val model : Value<Model>
    data class Model(
        val favType: FavScreenType,
        val pagingDataFlow : Flow<PagingData<Subscription>>,
        val subViewModel: SubViewModel,
        val backHandler: BackHandler
    )

    fun goToFavScreen()
}

class DefaultSubscriptionsComponent(
    componentContext: ComponentContext,
    favType : FavScreenType,
    val selectedFavScreen : (FavScreenType) -> Unit,
) : SubscriptionsComponent, ComponentContext by componentContext {

    private val subViewModel : SubViewModel = getKoin().get()

    private val _model = MutableValue(
        SubscriptionsComponent.Model(
            favType = favType,
            pagingDataFlow = subViewModel.init(),
            subViewModel = subViewModel,
            backHandler = backHandler
        )
    )

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        analyticsHelper.reportEvent("open_subscribes", mapOf())
    }

    override val model: Value<SubscriptionsComponent.Model> = _model

    override fun goToFavScreen() {
        selectedFavScreen(model.value.favType)
    }
}
