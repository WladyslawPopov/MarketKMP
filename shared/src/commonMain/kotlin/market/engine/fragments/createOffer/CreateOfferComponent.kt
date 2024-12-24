package market.engine.fragments.createOffer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.types.CreateOfferType
import org.koin.mp.KoinPlatform.getKoin

interface CreateOfferComponent {
    val model : Value<Model>

    data class Model(
        var catPath : List<Long>?,
        val offerId : Long?,
        val type : CreateOfferType,
        val externalImages : List<String>?,
        val createOfferViewModel: CreateOfferViewModel
    )

    fun onBackClicked()
}

class DefaultCreateOfferComponent(
    catPath : List<Long>?,
    offerId : Long?,
    type : CreateOfferType,
    externalImages : List<String>?,
    componentContext: ComponentContext,
    val navigateBack: () -> Unit
) : CreateOfferComponent, ComponentContext by componentContext {

    private val createOfferViewModel : CreateOfferViewModel = getKoin().get()

    private val _model = MutableValue(
        CreateOfferComponent.Model(
            catPath = catPath,
            offerId = offerId,
            type = type,
            externalImages = externalImages,
            createOfferViewModel = createOfferViewModel
        )
    )

    override val model = _model

    init {
        when(type){
            CreateOfferType.CREATE -> {
                createOfferViewModel.activeFiltersType.value = "categories"
                val searchData = SD()
                searchData.searchCategoryID = catPath?.get(0) ?: 1L
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
