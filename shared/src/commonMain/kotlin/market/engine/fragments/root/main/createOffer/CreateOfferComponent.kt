package market.engine.fragments.root.main.createOffer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.CreateOfferType
import org.koin.mp.KoinPlatform.getKoin

interface CreateOfferComponent {
    val model : Value<Model>

    data class Model(
        var catPath : List<Long>?,
        val offerId : Long?,
        val createOfferType : CreateOfferType,
        val externalImages : List<String>?,
        val createOfferViewModel: CreateOfferViewModel,
        val backHandler: BackHandler
    )

    fun onBackClicked()

    fun createNewOffer(offerId: Long? = null, type: CreateOfferType)

    fun goToOffer(id : Long)
}

class DefaultCreateOfferComponent(
    catPath : List<Long>?,
    offerId : Long?,
    type : CreateOfferType,
    externalImages : List<String>?,
    componentContext: ComponentContext,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToCreateOffer: (Long?, List<Long>?, CreateOfferType) -> Unit
) : CreateOfferComponent, ComponentContext by componentContext {

    private val createOfferViewModel : CreateOfferViewModel = getKoin().get()

    private val _model = MutableValue(
        CreateOfferComponent.Model(
            catPath = catPath,
            offerId = offerId,
            createOfferType = type,
            externalImages = externalImages,
            createOfferViewModel = createOfferViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        lifecycle.doOnResume {
            createOfferViewModel.updateUserInfo()

            if (UserData.token == ""){
                navigateBack()
            }
        }
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

    override fun createNewOffer(offerId: Long?, type: CreateOfferType) {
        navigateToCreateOffer(offerId, model.value.catPath, type)
    }

    override fun goToOffer(id: Long) {
        navigateToOffer(id)
    }
}
