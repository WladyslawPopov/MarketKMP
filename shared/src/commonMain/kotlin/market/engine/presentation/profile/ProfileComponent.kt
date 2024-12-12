package market.engine.presentation.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.globalData.UserData
import market.engine.core.items.ListingData
import org.koin.mp.KoinPlatform.getKoin

interface ProfileComponent {
    val model : Value<Model>

    data class Model(
        val profileViewModel: ProfileViewModel
    )

    fun navigateToMyOffers()

    fun updateProfile()

    fun goToAllMyOfferListing()
    fun goToAboutMe()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    val selectMyOffers: () -> Unit,
    val navigateToListing: (ListingData) -> Unit,
    val navigateToUser: (Long) -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
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

    override fun goToAllMyOfferListing() {
        val ld = ListingData()
        val searchData = ld.searchData.value
        searchData.userID = UserData.login
        searchData.userLogin = UserData.userInfo?.login
        searchData.userSearch = true
        ld.data.value.isOpenCategory.value = false
        navigateToListing(ld)
    }

    override fun goToAboutMe() {
        navigateToUser(UserData.login)
    }

    override fun navigateToMyOffers() {
        selectMyOffers()
    }
}


