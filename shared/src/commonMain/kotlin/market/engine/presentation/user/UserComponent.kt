package market.engine.presentation.user

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.koin.mp.KoinPlatform.getKoin

interface UserComponent {
    val model : Value<Model>

    data class Model(
        val userId: Long,
        val isClickedAboutMe: Boolean,
        val userViewModel: UserViewModel
    )

    fun navigateToMyOffers()
}

class DefaultUserComponent(
    userId: Long,
    isClickedAboutMe: Boolean = false,
    componentContext: ComponentContext,
    val selectMyOffers: () -> Unit
) : UserComponent, ComponentContext by componentContext {

    val _model = MutableValue(
        UserComponent.Model(
            userId = userId,
            isClickedAboutMe = isClickedAboutMe,
            userViewModel = getKoin().get()
        )
    )

    override val model = _model

    init {
        model.value.userViewModel.getUserInfo(userId)
    }

    override fun navigateToMyOffers() {
        selectMyOffers()
    }
}


