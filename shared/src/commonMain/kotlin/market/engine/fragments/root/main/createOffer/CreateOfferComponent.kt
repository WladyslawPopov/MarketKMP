package market.engine.fragments.root.main.createOffer

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.CreateOfferType

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

@OptIn(ExperimentalDecomposeApi::class)
class DefaultCreateOfferComponent(
    catPath : List<Long>?,
    offerId : Long?,
    type : CreateOfferType,
    externalImages : List<String>?,
    componentContext: JetpackComponentContext,
    val navigateBack: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToCreateOffer: (Long?, List<Long>?, CreateOfferType) -> Unit
) : CreateOfferComponent, JetpackComponentContext by componentContext {

    private val createOfferViewModel = viewModel("createOfferViewModel") {
        CreateOfferViewModel(
            catPath = catPath,
            offerId = offerId,
            type = type,
            externalImages = externalImages,
            component = this@DefaultCreateOfferComponent,
            savedStateHandle = createSavedStateHandle()
        )
    }

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
    }

    override fun onBackClicked() {
        createOfferViewModel.onBackClicked {
            navigateBack()
        }
    }

    override fun createNewOffer(offerId: Long?, type: CreateOfferType) {
        navigateToCreateOffer(offerId, model.value.catPath, type)
    }

    override fun goToOffer(id: Long) {
        navigateToOffer(id)
    }
}
