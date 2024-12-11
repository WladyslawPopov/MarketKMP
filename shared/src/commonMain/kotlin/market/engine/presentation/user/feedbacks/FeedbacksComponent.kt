package market.engine.presentation.user.feedbacks

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import market.engine.core.network.networkObjects.Reports
import market.engine.core.types.ReportPageType
import org.koin.mp.KoinPlatform.getKoin

interface FeedbacksComponent {
    val model : Value<Model>

    data class Model(
        val userId : Long,
        val type : ReportPageType,
        val feedbacksViewModel: FeedbacksViewModel,
        var pagingDataFlow : Flow<PagingData<Reports>>
    )

    fun onRefresh()
}

class DefaultFeedbacksComponent(
    val type : ReportPageType,
    val userId : Long,
    componentContext: ComponentContext,
) : FeedbacksComponent, ComponentContext by componentContext {

    private  val feedbacksViewModel : FeedbacksViewModel = getKoin().get()

    private val _model = MutableValue(
        FeedbacksComponent.Model(
            userId = userId,
            type = type,
            feedbacksViewModel = feedbacksViewModel,
            pagingDataFlow = feedbacksViewModel.init(type, userId)
        )
    )

    override val model = _model

    init {
        model.value.feedbacksViewModel.init(type,userId)
    }

    override fun onRefresh() {
        model.value.feedbacksViewModel.refresh()
    }
}
