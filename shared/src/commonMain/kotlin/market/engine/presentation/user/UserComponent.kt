package market.engine.presentation.user

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.items.ListingData
import market.engine.core.network.networkObjects.User
import org.koin.mp.KoinPlatform.getKoin

interface UserComponent {
    val model : Value<Model>

    data class Model(
        val userId: Long,
        val isClickedAboutMe: Boolean,
        val userViewModel: UserViewModel
    )

    fun updateUserInfo()

    fun selectAllOffers(user : User)

    fun onBack()
}

class DefaultUserComponent(
    userId: Long,
    isClickedAboutMe: Boolean = false,
    componentContext: ComponentContext,
    val goToListing: (ListingData) -> Unit,
    val navigateBack: () -> Unit
) : UserComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        UserComponent.Model(
            userId = userId,
            isClickedAboutMe = isClickedAboutMe,
            userViewModel = getKoin().get()
        )
    )

    override val model = _model

    init {
        updateUserInfo()
    }

    override fun updateUserInfo() {
        model.value.userViewModel.getUserInfo(model.value.userId)
    }

    override fun selectAllOffers(user : User) {
        val ld = ListingData()
        val searchData = ld.searchData.value
        searchData.userID = user.id
        searchData.userSearch = true
        searchData.userLogin = user.login
        ld.data.value.isOpenCategory.value = false
        goToListing(ld)
    }

    override fun onBack() {
        navigateBack()
    }
}


