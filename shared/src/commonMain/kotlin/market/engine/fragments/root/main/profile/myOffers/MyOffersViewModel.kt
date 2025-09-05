package market.engine.fragments.root.main.profile.myOffers

import androidx.lifecycle.SavedStateHandle

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.states.CategoryState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.CabinetOfferRepository
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

class MyOffersViewModel(
    val type: LotsType,
    val component: MyOffersComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingBaseViewModel = component.additionalModels.value.listingBaseViewModel
    private val categoryViewModel = component.additionalModels.value.categoryViewModel

    val ld = listingBaseViewModel.listingData
    val activeType = listingBaseViewModel.activeWindowType

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    val categoryState = CategoryState(
                        activeType.value == ActiveWindowListingType.CATEGORY_FILTERS,
        categoryViewModel
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<CabinetOfferRepository>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Offer.serializer()
            ){ tc ->
                listingBaseViewModel.setTotalCount(tc)
            }.map { pagingData ->
                pagingData.map { offer ->
                    CabinetOfferRepository(
                        offer,
                        listingParams,
                        OfferRepositoryEventsImpl(this, component),
                        this
                    )
                }
            }
        }.cachedIn(scope)

    init {
        scope.launch {
            listingBaseViewModel.setListingData(
                listingBaseViewModel.listingData.value.copy(
                    data = LD(
                        filters = OfferFilters.getByTypeFilter(type),
                        methodServer = "get_cabinet_listing",
                        objServer = "offers"
                    )
                )
            )
            listingBaseViewModel.setListItemsFilterBar(
                buildList {
                    val filterString = getString(strings.filter)
                    val sortString = getString(strings.sort)
                    val filters = ld.value.data.filters.filter {
                        it.value != "" &&
                                it.interpretation?.isNotBlank() == true
                    }

                    add(
                        NavigationItem(
                            title = filterString,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            hasNews = ld.value.data.sort != null,
                            badgeCount = null,
                        )
                    )
                }
            )
        }
    }

    fun onBackNavigation(onBack: () -> Unit){
        when(activeType.value){
            ActiveWindowListingType.CATEGORY_FILTERS -> {
                if (categoryState.categoryViewModel.searchData.value.searchCategoryID != 1L){
                    categoryState.categoryViewModel.navigateBack()
                }else{
                    listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
                }
            }
            else -> {
                onBack()
            }
        }
    }
}

data class OfferRepositoryEventsImpl(
    val viewModel: MyOffersViewModel,
    val component: MyOffersComponent
) : OfferRepositoryEvents {
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {
        component.goToCreateOffer(type, id, catpath)
    }

    override fun goToProposalPage(
        offerId: Long,
        type: ProposalType
    ) {
        component.goToProposals(offerId, type)
    }

    override fun goToDynamicSettings(type: String, id: Long) {
        component.goToDynamicSettings(type, id)
    }

    override fun goToDialog(id: Long?) {}
    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {}
    override fun goToUserPage(sellerId: Long) {}

    override fun openCabinetOffer(offer: OfferItem) {
        val selectedItems = viewModel.listingBaseViewModel.selectItems
        if (selectedItems.value.isNotEmpty()) {
            if (selectedItems.value.contains(offer.id)) {
                viewModel.listingBaseViewModel.removeSelectItem(offer.id)
            } else {
                viewModel.listingBaseViewModel.addSelectItem(offer.id)
            }
        } else {
            component.goToOffer(offer)
        }
    }

    override fun scrollToBids() {}
    override fun refreshPage() {}
}
