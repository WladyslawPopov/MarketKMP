package market.engine.fragments.root.main.messenger

import com.arkivanov.decompose.ComponentContext
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

interface DialogsComponent{
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

class DefaultDialogsComponent(
    componentContext: ComponentContext,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToOrder: (Long, DealTypeGroup) -> Unit,
    val navigateToListingSelected: (ListingData) -> Unit,
    dialogId : Long,
    message : String?,
) : DialogsComponent, ComponentContext by componentContext {

    private val dialogsViewModel : DialogsViewModel = DialogsViewModel(dialogId,message, this)

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
