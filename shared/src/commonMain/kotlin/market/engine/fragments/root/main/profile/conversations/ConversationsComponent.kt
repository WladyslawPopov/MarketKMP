package market.engine.fragments.root.main.profile.conversations

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Conversations

interface ConversationsComponent {
    val model : Value<Model>
    data class Model(
        var message : String?,
        val viewModel: ConversationsViewModel,
        val backHandler: BackHandler
    )

    fun goToMessenger(conversation : Conversations)
    fun onBack()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultConversationsComponent(
    copyMessage: String?,
    componentContext: JetpackComponentContext,
    val navigateBack : () -> Unit,
    val navigateToMessenger : (Long, String?) -> Unit,
) : ConversationsComponent, JetpackComponentContext by componentContext {

    private val viewModel : ConversationsViewModel = ConversationsViewModel(this)

    private val _model = MutableValue(
        ConversationsComponent.Model(
            message = copyMessage,
            viewModel = viewModel,
            backHandler = backHandler
        )
    )
    override val model: Value<ConversationsComponent.Model> = _model

    private val updateBackHandlerItem = MutableValue(1L)

    init {
        lifecycle.doOnResume {
            viewModel.updateUserInfo()
            if (UserData.token == ""){
                navigateBack()
            }
            if (updateBackHandlerItem.value != 1L) {
                viewModel.setUpdateItem(updateBackHandlerItem.value)
                updateBackHandlerItem.value = 1L
            }
        }
    }


    override fun goToMessenger(conversation : Conversations){
        if (conversation.countUnreadMessages > 0){
            viewModel.markReadConversation(conversation.id)
        }
        updateBackHandlerItem.value = conversation.id
        navigateToMessenger(conversation.id, model.value.message)
        model.value.message = null
    }

    override fun onBack() {
        navigateBack()
    }
}
