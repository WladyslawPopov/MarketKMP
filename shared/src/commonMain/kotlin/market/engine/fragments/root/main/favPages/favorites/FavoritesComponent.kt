package market.engine.fragments.root.main.favPages.favorites

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.AnalyticsFactory
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.setNewParams
import market.engine.fragments.root.main.favPages.FavPagesViewModel
import kotlin.collections.contains

interface FavoritesComponent {
    val model : Value<Model>
    data class Model(
        val listId : Long?,
        val favType: FavScreenType,
        val pagingDataFlow : Flow<PagingData<OfferItem>>,
        val favViewModel: FavPagesViewModel,
        val backHandler: BackHandler
    )

    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun onRefresh()
    fun refreshTabs()
    fun goToProposal(type: ProposalType, offerId : Long)
    fun goToCreateOffer(createOfferType : CreateOfferType, id : Long)
    fun deleteSelectsItems(ids: List<Long>)
    fun isHideItem(offer: OfferItem): Boolean
    fun updateItem(oldItem : OfferItem?)
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    favType : FavScreenType,
    idList : Long?,
    val goToOffer : (Long) -> Unit,
    val updateTabs : () -> Unit,
    val navigateToProposalPage : (ProposalType, Long) -> Unit,
    val navigateToCreateOffer : (CreateOfferType, Long) -> Unit,
) : FavoritesComponent, ComponentContext by componentContext {

    private val favViewModel : FavPagesViewModel = FavPagesViewModel()

    val listingData = favViewModel.listingData.value

    private val _model = MutableValue(
        FavoritesComponent.Model(
            listId = idList,
            favType = favType,
            favViewModel = favViewModel,
            pagingDataFlow = favViewModel.init(favType, idList),
            backHandler = backHandler
        )
    )

    override val model: Value<FavoritesComponent.Model> = _model

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            favViewModel.updateUserInfo()
        }

        analyticsHelper.reportEvent("open_favorites", mapOf("type" to favType.name))
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
        goToOffer(offer.id)
        lifecycle.doOnResume {
            favViewModel.updateItem.value = offer.id
        }
    }

    override fun onRefresh() {
        favViewModel.onError(ServerErrorException())
        favViewModel.updateUserInfo()
        favViewModel.resetScroll()
        favViewModel.refresh()
        favViewModel.updateFilters.value++
    }

    override fun refreshTabs() {
        updateTabs()
    }

    override fun goToProposal(type: ProposalType, offerId: Long) {
        navigateToProposalPage(type, offerId)
    }

    override fun goToCreateOffer(createOfferType: CreateOfferType, id: Long) {
        navigateToCreateOffer(createOfferType, id)
    }

    override fun deleteSelectsItems(ids: List<Long>) {
        favViewModel.viewModelScope.launch {
            ids.forEach { item ->
                val body = HashMap<String, JsonElement>()

                val type = when (model.value.favType) {
                    FavScreenType.NOTES -> {
                        "delete_note"
                    }

                    FavScreenType.FAVORITES -> {
                        "unwatch"
                    }

                    FavScreenType.FAV_LIST -> {
                        body["offers_list_id"] = JsonPrimitive(model.value.listId)
                        "remove_from_list"
                    }

                    else -> {
                        ""
                    }
                }

                favViewModel.postOperationFields(
                    item,
                    type,
                    "offers",
                    body,
                    onSuccess = {
                        favViewModel.selectItems.clear()
                        onRefresh()
                    },
                    errorCallback = {

                    }
                )
            }
        }
    }

    override fun isHideItem(offer: OfferItem): Boolean {
        return when (model.value.favType) {
            FavScreenType.NOTES -> {
                offer.note == null && offer.note == ""
            }

            FavScreenType.FAVORITES -> {
                !offer.isWatchedByMe
            }

            else -> {
                offer.session == null
            }
        }
    }

    override fun updateItem(oldItem : OfferItem?) {
        favViewModel.viewModelScope.launch {
            model.value.listId?.let { id ->
                favViewModel.getList(id) { item ->
                    if (!item.offers.contains(oldItem?.id)) {
                        oldItem?.session = null
                    }
                }
            }

            val offer = withContext(Dispatchers.IO) {
                favViewModel.getOfferById(favViewModel.updateItem.value!!)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    oldItem?.setNewParams(offer)
                }

                if (model.value.favType != FavScreenType.FAV_LIST) {
                    if (oldItem != null) {
                        val isHide = isHideItem(oldItem)
                        if (isHide) {
                            onRefresh()
                        }
                    }
                } else {
                    favViewModel.getList(model.value.listId ?: 1L) {
                        val isEmpty = oldItem?.let { item ->
                            it.offers.contains(item.id)
                        }
                        if (isEmpty == true) {
                            onRefresh()
                        }
                    }
                }

                favViewModel.updateItemTrigger.value++
                favViewModel.updateItem.value = null
            }
        }
    }
}
