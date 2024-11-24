package market.engine.presentation.offer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.repositories.UserRepository
import org.koin.mp.KoinPlatform.getKoin

interface OfferComponent {

    val model : Value<Model>

    data class Model(
        val id: Long,
        val isSnapshot: Boolean,
        val offerViewModel: OfferViewModel
    )
    fun updateOffer(id: Long)
    fun navigateToOffers(id: Long)
    fun onBeakClick()
}

class DefaultOfferComponent(
    val id: Long,
    isSnapshot: Boolean,
    componentContext: ComponentContext,
    val selectOffer: (id: Long) -> Unit,
    val navigationBack: () -> Unit
) : OfferComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        OfferComponent.Model(
            id = id,
            isSnapshot = isSnapshot,
            offerViewModel = getKoin().get()
        )
    )
    override val model: Value<OfferComponent.Model> = _model
    private val offerViewModel = model.value.offerViewModel
    private val userRepository = getKoin().get<UserRepository>()

    init {
        userRepository.updateToken()
        userRepository.updateUserInfo(offerViewModel.viewModelScope)
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


