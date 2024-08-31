package application.market.auction_mobile.ui.home

import application.market.auction_mobile.business.networkObjects.Category
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


interface HomeComponent {
    val model: Value<Model>

    fun onItemClicked(id: Long)

    data class Model(
        val categories: StateFlow<List<Category>>
    )
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val onItemSelected: (id: Long) -> Unit,
    homeViewModel: HomeViewModel
) : HomeComponent, ComponentContext by componentContext {
    private val _model = MutableValue(HomeComponent.Model(categories = MutableStateFlow(emptyList())))
    override val model: Value<HomeComponent.Model> = _model

    init {
        CoroutineScope(Dispatchers.Default).launch {
            homeViewModel.categories.collect {
                _model.value = HomeComponent.Model(categories = homeViewModel.categories)
            }
        }
    }

    override fun onItemClicked(id: Long) {
        onItemSelected(id)
    }
}


