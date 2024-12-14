package market.engine.presentation.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.globalData.UserData
import market.engine.core.items.ListingData
import market.engine.core.items.NavigationItem
import org.koin.mp.KoinPlatform.getKoin

interface ProfileComponent {
    val model : Value<Model>

    data class Model(
        val navigationItems: List<NavigationItem>,
        val profileViewModel: ProfileViewModel
    )

    fun navigateToMyOffers()

    fun updateProfile()
    fun goToMyProfile()
    fun goToAllMyOfferListing()
    fun goToAboutMe()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    navigationItems: List<NavigationItem>,
    val selectMyOffers: () -> Unit,
    val navigateToListing: (ListingData) -> Unit,
    val navigateToUser: (Long, Boolean) -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        ProfileComponent.Model(
            navigationItems = navigationItems,
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

    override fun goToMyProfile() {
        navigateToUser(UserData.login, false)
    }

    override fun goToAllMyOfferListing() {
        val ld = ListingData()
        val searchData = ld.searchData.value
        searchData.userID = UserData.login
        searchData.userLogin = UserData.userInfo?.login
        searchData.userSearch = true
        navigateToListing(ld)
    }

    override fun goToAboutMe() {
        navigateToUser(UserData.login, true)
    }

    override fun navigateToMyOffers() {
        selectMyOffers()
    }
}


