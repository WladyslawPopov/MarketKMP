package market.engine.fragments.root.main.profile.conversations

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.network.networkObjects.Conversations
import org.koin.mp.KoinPlatform.getKoin


interface ConversationsComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<Conversations>>,
        val viewModel: ConversationsViewModel,
        val navigationItems : List<NavigationItem>,
        val selectedId : Long?,
        val backHandler: BackHandler
    )

    fun goToMessenger(conversation : Conversations)
    fun onBack()
}

class DefaultConversationsComponent(
    componentContext: ComponentContext,
    navigationItems : List<NavigationItem>,
    val navigateBack : () -> Unit,
    val navigateToMessenger : (Long) -> Unit,
    private var selectedId : Long? = null
) : ConversationsComponent, ComponentContext by componentContext {

    private val viewModel : ConversationsViewModel = getKoin().get()

    private val _model = MutableValue(
        ConversationsComponent.Model(
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            navigationItems = navigationItems,
            selectedId = selectedId,
            backHandler = backHandler
        )
    )
    override val model: Value<ConversationsComponent.Model> = _model

    override fun goToMessenger(conversation : Conversations){
        if (conversation.countUnreadMessages > 0){
            viewModel.markReadConversation(conversation.id)
        }
        lifecycle.doOnResume {
            viewModel.updateItem.value = conversation.id
        }
        navigateToMessenger(conversation.id)
    }

    override fun onBack() {
        navigateBack()
    }

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            viewModel.updateUserInfo()
            if (UserData.token == ""){
                navigateBack()
            }
        }
        val eventParameters = mapOf(
            "user_id" to UserData.login.toString(),
            "profile_source" to "messages"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)

        if (selectedId != null) {
            lifecycle.doOnResume {
                viewModel.updateItem.value = selectedId
            }
            navigateToMessenger(selectedId!!)
            selectedId = null
        }
    }
}
