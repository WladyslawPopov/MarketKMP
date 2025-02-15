package market.engine.fragments.root.main.messenger

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DialogsData
import market.engine.core.data.types.DealTypeGroup
import org.koin.mp.KoinPlatform.getKoin


interface DialogsComponent{
    val model : Value<Model>

    data class Model(
        val dialogId : Long,
        val pagingDataFlow : Flow<PagingData<DialogsData>>,
        val dialogsViewModel: DialogsViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()
    fun goToOffer(id : Long)
    fun goToUser(id : Long)
    fun goToOrder(id : Long, dealTypeGroup: DealTypeGroup)
}

class DefaultDialogsComponent(
    componentContext: ComponentContext,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToOrder: (Long, DealTypeGroup) -> Unit,
    dialogId : Long
) : DialogsComponent, ComponentContext by componentContext {

    private val dialogsViewModel : DialogsViewModel = DialogsViewModel(
        getKoin().get(),
        getKoin().get(),
        getKoin().get(),
    )

    private val _model = MutableValue(
        DialogsComponent.Model(
            dialogId = dialogId,
            dialogsViewModel = dialogsViewModel,
            pagingDataFlow = dialogsViewModel.init(dialogId),
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        lifecycle.doOnResume {
            dialogsViewModel.updateUserInfo()

            if (UserData.token == ""){
                navigateBack()
            }
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
}
