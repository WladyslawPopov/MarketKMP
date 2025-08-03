package market.engine.fragments.root.main.profile.myProposals

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
import market.engine.core.repositories.OfferRepository
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.getMainTread
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.fragments.root.DefaultRootComponent
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString

class MyProposalsViewModel(
    val type: LotsType,
    val component: MyProposalsComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingBaseViewModel = ListingBaseViewModel(savedStateHandle = savedStateHandle)
    val ld = listingBaseViewModel.listingData
    val activeType = listingBaseViewModel.activeWindowType

    val categoryState = CategoryState(
        activeType.value == ActiveWindowListingType.CATEGORY_FILTERS,
        CategoryViewModel(isFilters = true, savedStateHandle = savedStateHandle)
    )

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    private val filtersCategoryModel = CategoryViewModel(
        isFilters = true,
        savedStateHandle = savedStateHandle
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<OfferRepository>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Offer.serializer()
            ){ tc ->
                listingBaseViewModel.setTotalCount(tc)
            }.map { pagingData ->
                pagingData.map { offer ->
                    OfferRepository(
                        offer= offer,
                        listingParams,
                        events = OfferRepositoryEventsImpl(this, component),
                        this,
                    )
                }
            }
        }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            listingBaseViewModel.setListingData(
                ListingData(
                    data = LD(
                        filters = OfferFilters.getByTypeFilter(type),
                        methodServer = "get_cabinet_listing_my_price_proposals",
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
                if (filtersCategoryModel.searchData.value.searchCategoryID != 1L){
                    filtersCategoryModel.navigateBack()
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
    val viewModel: MyProposalsViewModel,
    val component: MyProposalsComponent
): OfferRepositoryEvents {

    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {
    }

    override fun goToProposalPage(offerId: Long, type: ProposalType) {
        viewModel.getMainTread {
            component.goToProposal(offerId, type)
        }
    }

    override fun goToDynamicSettings(type: String, id: Long) {
        viewModel.getMainTread {
            DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
        }
    }

    override fun goToLogin() {
        viewModel.getMainTread {
            DefaultRootComponent.Companion.goToLogin(false)
        }
    }

    override fun goToDialog(id: Long?) {
        viewModel.getMainTread {
            component.goToDialog(id)
        }
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {}

    override fun goToUserPage(sellerId : Long) {
        viewModel.getMainTread {
            component.goToUser(sellerId)
        }
    }

    override fun openCabinetOffer(offer: OfferItem) {
        viewModel.getMainTread {
            component.goToOffer(offer)
        }
    }

    override fun scrollToBids() {}
    override fun refreshPage() {}
    override fun updateBidsInfo(item: OfferItem) {}
}
