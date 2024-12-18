package market.engine.presentation.createOffer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.types.CreateOfferTypes
import org.koin.mp.KoinPlatform.getKoin

interface CreateOfferComponent {

    val model : Value<Model>

    data class Model(
        var categoryId : Long,
        val offerId : Long?,
        val type : CreateOfferTypes,
        val externalImages : List<String>?,
        val createOfferViewModel: CreateOfferViewModel
    )

    fun onBackClicked()
}

class DefaultCreateOfferComponent(
    categoryId : Long,
    offerId : Long?,
    type : CreateOfferTypes,
    externalImages : List<String>?,
    componentContext: ComponentContext,
    val navigateBack: () -> Unit
) : CreateOfferComponent, ComponentContext by componentContext {

    private val createOfferViewModel : CreateOfferViewModel = getKoin().get()

    private val _model = MutableValue(
        CreateOfferComponent.Model(
            categoryId = categoryId,
            offerId = offerId,
            type = type,
            externalImages = externalImages,
            createOfferViewModel = createOfferViewModel
        )
    )

    override val model = _model

    init {
        when(type){
            CreateOfferTypes.CREATE -> {
                createOfferViewModel.activeFiltersType.value = "categories"
                val searchData = SD()
                searchData.searchCategoryID = categoryId
                createOfferViewModel.getCategories(searchData, LD(), withoutCounter = true)
            }
            else ->{
                createOfferViewModel.activeFiltersType.value = ""
            }
        }
    }

    override fun onBackClicked() {
        navigateBack()
    }
}
