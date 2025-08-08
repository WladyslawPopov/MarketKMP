package market.engine.fragments.root.main.home

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.utils.deleteReadNotifications
import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.essenty.backhandler.BackCallback


interface HomeComponent {
    val model: Value<Model>
    data class Model(
        val homeViewModel: HomeViewModel,
        val backHandler: BackHandler,
    )

    fun goToCreateOffer()
    fun goToMessenger()
    fun goToContactUs()
    fun goToAppSettings()
    fun goToMyProposals()
    fun goToNotificationHistory()
    fun goToLogin()
    fun goToOffer(id: Long)
    fun goToNewSearch(listingData: ListingData = ListingData(), search: Boolean = true)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultHomeComponent(
    componentContext: JetpackComponentContext,
    private val navigateToListingSelected: (ListingData, Boolean) -> Unit,
    val navigateToLoginSelected: () -> Unit,
    val navigateToOfferSelected: (id: Long) -> Unit,
    val navigateToCreateOfferSelected: () -> Unit,
    val navigateToMessengerSelected: () -> Unit,
    val navigateToContactUsSelected: () -> Unit,
    val navigateToSettingsSelected: () -> Unit,
    val navigateToMyProposalsSelected: () -> Unit,
    val navigateToNotificationHistorySelected: () -> Unit,
) : HomeComponent, JetpackComponentContext by componentContext {

    private val homeViewModel: HomeViewModel = viewModel("homeViewModel") {
        HomeViewModel(this@DefaultHomeComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        HomeComponent.Model(
            homeViewModel,
            backHandler,
        )
    )

    override val model: Value<HomeComponent.Model> = _model

    val backCallback = BackCallback {

    }

    init {
        model.value.backHandler.register(backCallback)
        lifecycle.doOnResume {
            deleteReadNotifications()
            homeViewModel.updatePage()
        }
    }

    override fun goToCreateOffer() {
        navigateToCreateOfferSelected()
    }

    override fun goToMessenger() {
        navigateToMessengerSelected()
    }

    override fun goToContactUs() {
        navigateToContactUsSelected()
    }

    override fun goToAppSettings() {
        navigateToSettingsSelected()
    }

    override fun goToMyProposals() {
        navigateToMyProposalsSelected()
    }

    override fun goToNotificationHistory() {
        navigateToNotificationHistorySelected()
    }

    override fun goToLogin() {
        navigateToLoginSelected()
    }

    override fun goToOffer(id: Long) {
        navigateToOfferSelected(id)
    }

    override fun goToNewSearch(listingData: ListingData, search: Boolean) {
        navigateToListingSelected(listingData, search)
    }
}
