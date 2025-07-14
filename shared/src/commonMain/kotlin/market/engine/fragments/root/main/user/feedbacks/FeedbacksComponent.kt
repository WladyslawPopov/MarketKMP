package market.engine.fragments.root.main.user.feedbacks

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ReportPageType

interface FeedbacksComponent {
    val model : Value<Model>

    data class Model(
        val userId : Long,
        var type : ReportPageType,
        val feedbacksViewModel: FeedbacksViewModel
    )

    fun goToOrder(orderId: Long, type: DealTypeGroup)

    fun goToSnapshot(snapshotId : Long)

    fun goToUser(userId : Long)
}

class DefaultFeedbacksComponent(
    val type : ReportPageType,
    val userId : Long,
    componentContext: ComponentContext,
    private val navigateToOrder : (Long, DealTypeGroup) -> Unit,
    private val navigateToSnapshot : (Long) -> Unit,
    private val navigateToUser : (Long) -> Unit,
) : FeedbacksComponent, ComponentContext by componentContext {

    val feedbacksViewModel = FeedbacksViewModel(type, userId)

    private val _model = MutableValue(
        FeedbacksComponent.Model(
            userId = userId,
            type = type,
            feedbacksViewModel = feedbacksViewModel
        )
    )

    override val model = _model

    override fun goToOrder(orderId: Long, type: DealTypeGroup) {
        navigateToOrder(orderId, type)
    }

    override fun goToSnapshot(snapshotId: Long) {
        navigateToSnapshot(snapshotId)
    }

    override fun goToUser(userId: Long) {
        navigateToUser(userId)
    }
}
