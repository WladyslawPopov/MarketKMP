package market.engine.fragments.root.main.createSubscription

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.widgets.filterContents.categories.CategoryViewModel

interface CreateSubscriptionComponent {
    val additionalModels : Value<AdditionalModel>
    data class AdditionalModel(
        val categoryViewModel: CategoryViewModel
    )

    val model : Value<Model>

    data class Model(
        val editId : Long? = null,
        val createSubscriptionViewModel: CreateSubscriptionViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultCreateSubscriptionComponent(
    componentContext: JetpackComponentContext,
    editId : Long?,
    val navigateBack: () -> Unit,
) : CreateSubscriptionComponent, JetpackComponentContext by componentContext {

    private val categoryViewModel = viewModel("CreateSubscriptionCategoryViewModel") {
        CategoryViewModel(
            isFilters = true,
            savedStateHandle = createSavedStateHandle()
        )
    }

    override val additionalModels: Value<CreateSubscriptionComponent.AdditionalModel>
        get() = MutableValue(
            CreateSubscriptionComponent.AdditionalModel(
                categoryViewModel = categoryViewModel
            )
        )

    private val createSubscriptionViewModel = viewModel("CreateSubscriptionViewModel") {
        CreateSubscriptionViewModel(editId, this@DefaultCreateSubscriptionComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        CreateSubscriptionComponent.Model(
            editId = editId,
            createSubscriptionViewModel = createSubscriptionViewModel,
            backHandler = backHandler
        )
    )
    override val model = _model

    override fun onBackClicked() {
        createSubscriptionViewModel.onBack {
            navigateBack()
        }
    }
}
