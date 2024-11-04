package market.engine.presentation.subscriptions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.analytics.AnalyticsHelper
import org.koin.mp.KoinPlatform.getKoin


interface SubscribesComponent {
    val model : Value<Model>
    data class Model(
        val subViewModel: SubViewModel
    )

    fun goToFavorites()
    fun onRefresh()
}

class DefaultSubscribesComponent(
    componentContext: ComponentContext,
    val selectedFavorites : () -> Unit,
) : SubscribesComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        SubscribesComponent.Model(
            subViewModel = getKoin().get()
        )
    )
    override val model: Value<SubscribesComponent.Model> = _model
    override fun goToFavorites() {
        selectedFavorites()
    }

    private val listingViewModel = model.value.subViewModel
    private val analyticsHelper = getKoin().get<AnalyticsHelper>()


    override fun onRefresh() {
        listingViewModel.firstVisibleItemScrollOffset = 0
        listingViewModel.firstVisibleItemIndex = 0
        listingViewModel.updateCurrentListingData()

        analyticsHelper.reportEvent("open_subscribes", "")
    }
}
