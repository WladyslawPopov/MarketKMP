package market.engine.presentation.offer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.globalData.UserData
import org.koin.mp.KoinPlatform.getKoin
import kotlin.coroutines.coroutineContext

interface OfferComponent {

    val model : Value<Model>

    data class Model(
        val id: Long,
        val offerViewModel: OfferViewModel
    )
    fun updateOffer(id: Long)
    fun navigateToOffers(id: Long)
    fun onBeakClick()
}

class DefaultOfferComponent(
    val id: Long,
    componentContext: ComponentContext,
    val selectOffer: (id: Long) -> Unit,
    val navigationBack: () -> Unit
) : OfferComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        OfferComponent.Model(
            id = id,
            offerViewModel = getKoin().get()
        )
    )
    override val model: Value<OfferComponent.Model> = _model
    private val offerViewModel = model.value.offerViewModel

    init {
        updateOffer(id)
    }

    override fun updateOffer(id: Long) {
        offerViewModel.getOffer(id)
    }

    override fun navigateToOffers(id: Long) {
        selectOffer(id)
    }

    override fun onBeakClick() {
        navigationBack()
    }
}


