package market.engine.fragments.root.main.profile.conversations

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
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
        val navigationItems : List<NavigationItem>
    )
}

class DefaultConversationsComponent(
    componentContext: ComponentContext,
    navigationItems : List<NavigationItem>,
) : ConversationsComponent, ComponentContext by componentContext {

    private val viewModel : ConversationsViewModel = getKoin().get()

    private val _model = MutableValue(
        ConversationsComponent.Model(
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            navigationItems = navigationItems
        )
    )
    override val model: Value<ConversationsComponent.Model> = _model
    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        viewModel.updateUserInfo()
        val eventParameters = mapOf(
            "user_id" to UserData.login.toString(),
            "profile_source" to "messages"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
    }
}
