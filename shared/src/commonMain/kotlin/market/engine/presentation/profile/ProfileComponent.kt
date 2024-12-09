package market.engine.presentation.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.globalData.UserData
import org.koin.mp.KoinPlatform.getKoin

interface ProfileComponent {
    val model : Value<Model>

    data class Model(
        val profileViewModel: ProfileViewModel
    )

    fun navigateToMyOffers()

    fun updateProfile()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    val selectMyOffers: () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    val _model = MutableValue(
        ProfileComponent.Model(
            profileViewModel = getKoin().get()
        )
    )
    override val model = _model


    init {
        updateProfile()
    }

    override fun updateProfile() {
        model.value.profileViewModel.getUserInfo(UserData.login)
    }

    override fun navigateToMyOffers() {
        selectMyOffers()
    }
}


