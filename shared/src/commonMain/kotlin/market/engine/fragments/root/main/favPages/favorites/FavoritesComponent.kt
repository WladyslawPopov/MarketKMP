package market.engine.fragments.root.main.favPages.favorites

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.ProposalType
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel

interface FavoritesComponent {

    val additionalModels : Value<AdditionalModels>
    data class AdditionalModels(
        val listingBaseViewModel: ListingBaseViewModel,
        val categoryViewModel: CategoryViewModel,
    )

    val model : Value<Model>
    data class Model(
        val listId : Long?,
        val favType: FavScreenType,
        val favViewModel: FavViewModel,
        val backHandler: BackHandler
    )

    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun refreshTabs()
    fun goToProposal(type: ProposalType, offerId : Long)
    fun goToCreateOffer(createOfferType : CreateOfferType, id : Long)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultFavoritesComponent(
    componentContext: JetpackComponentContext,
    favType : FavScreenType,
    idList : Long?,
    val goToOffer : (Long) -> Unit,
    val updateTabs : () -> Unit,
    val navigateToProposalPage : (ProposalType, Long) -> Unit,
    val navigateToCreateOffer : (CreateOfferType, Long) -> Unit,
) : FavoritesComponent, JetpackComponentContext by componentContext {

    val listingBaseVM = viewModel("favoritesBaseViewModel"){
        ListingBaseViewModel(
            deleteSelectedItems = {
                model.value.favViewModel.deleteSelectsItems()
            },
            savedStateHandle = createSavedStateHandle()
        )
    }

    val listingCategoryModel = viewModel("favoritesCategoryViewModel"){
        CategoryViewModel(savedStateHandle = createSavedStateHandle())
    }

    private val _additionalModels = MutableValue(
        FavoritesComponent.AdditionalModels(
            listingBaseVM, listingCategoryModel
        )
    )

    override val additionalModels: Value<FavoritesComponent.AdditionalModels> = _additionalModels

    private val favViewModel = viewModel {
        FavViewModel(favType, idList, this@DefaultFavoritesComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        FavoritesComponent.Model(
            listId = idList,
            favType = favType,
            favViewModel = favViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<FavoritesComponent.Model> = _model

    private val updateBackHandlerItem = MutableValue(1L)

    init {
        lifecycle.doOnResume {
            favViewModel.scope.launch(Dispatchers.IO) {
                favViewModel.updateUserInfo()
            }

            if (updateBackHandlerItem.value != 1L) {
                favViewModel.setUpdateItem(updateBackHandlerItem.value)
                updateBackHandlerItem.value = 1L
            }
        }

        lifecycle.doOnDestroy {
            favViewModel.onClear()
            listingCategoryModel.onClear()
        }
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
        goToOffer(offer.id)
        lifecycle.doOnResume {
            favViewModel.setUpdateItem(offer.id)
        }
    }

    override fun refreshTabs() {
        updateTabs()
    }

    override fun goToProposal(type: ProposalType, offerId: Long) {
        navigateToProposalPage(type, offerId)
    }

    override fun goToCreateOffer(createOfferType: CreateOfferType, id: Long) {
        navigateToCreateOffer(createOfferType, id)
        updateBackHandlerItem.value = id
    }

}
