package market.engine.fragments.root.main.messenger

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.activeDialog
import market.engine.core.data.globalData.updateMessenger
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.base.listing.ListingBaseViewModel

interface DialogsComponent {

    val additionalModels : Value<AdditionalModels>
    data class AdditionalModels(
        val listingBaseViewModel: ListingBaseViewModel
    )

    val model : Value<Model>
    data class Model(
        val dialogId : Long,
        val dialogsViewModel: DialogsViewModel,
        val backHandler: BackHandler,
    )

    fun onBackClicked()
    fun goToOffer(id : Long)
    fun goToUser(id : Long)
    fun goToOrder(id : Long, dealTypeGroup: DealTypeGroup)
    fun goToNewSearch(userId: Long)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultDialogsComponent(
    componentContext: JetpackComponentContext,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToOrder: (Long, DealTypeGroup) -> Unit,
    val navigateToListingSelected: (ListingData) -> Unit,
    dialogId : Long,
    message : String?,
) : DialogsComponent, JetpackComponentContext by componentContext {

    val listingBaseVM = viewModel("dialogsBaseViewModel") {
        ListingBaseViewModel(savedStateHandle = createSavedStateHandle())
    }

    private val _additionalModels = MutableValue(
        DialogsComponent.AdditionalModels(
            listingBaseVM
        )
    )

    override val additionalModels: Value<DialogsComponent.AdditionalModels> = _additionalModels

    private val dialogsViewModel = viewModel("dialogsViewModel") {
        DialogsViewModel(dialogId, message, this@DefaultDialogsComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        DialogsComponent.Model(
            dialogId = dialogId,
            dialogsViewModel = dialogsViewModel,
            backHandler = backHandler
        )
    )


    override val model = _model

    init {
        //global foreground update param
        activeDialog = dialogId
        updateMessenger =  { dialogsViewModel.updatePage() }

        lifecycle.doOnResume {
            dialogsViewModel.updateUserInfo()

            if (UserData.token == ""){
                navigateBack()
            }
        }

        lifecycle.doOnDestroy {
            updateMessenger = null
            activeDialog = null
        }

        dialogsViewModel.analyticsHelper.reportEvent("view_dialogs", mapOf())
    }

    override fun onBackClicked() {
        navigateBack()
    }

    override fun goToOffer(id: Long) {
        navigateToOffer(id)
    }

    override fun goToUser(id: Long) {
        navigateToUser(id)
    }

    override fun goToOrder(id: Long, dealTypeGroup: DealTypeGroup) {
        navigateToOrder(id, dealTypeGroup)
    }

    override fun goToNewSearch(userId: Long) {
        val listingData = ListingData()
        listingData.searchData.run {
            userSearch = true
            userLogin = ""
            userID = userId
        }
        navigateToListingSelected(listingData)
    }
}
