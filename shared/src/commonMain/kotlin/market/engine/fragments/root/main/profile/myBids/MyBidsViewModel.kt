package market.engine.fragments.root.main.profile.myBids

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
import market.engine.core.data.globalData.UserData
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
import market.engine.fragments.root.DefaultRootComponent
import org.jetbrains.compose.resources.getString

class MyBidsViewModel(
    val type: LotsType,
    val component: MyBidsComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingBaseViewModel = component.additionalModels.value.listingBaseViewModel
    private val categoryViewModel = component.additionalModels.value.categoryViewModel

    val ld = listingBaseViewModel.listingData
    val activeType = listingBaseViewModel.activeWindowType

    val categoryState = CategoryState(
        activeType.value == ActiveWindowListingType.CATEGORY_FILTERS,
        categoryViewModel
    )

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

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
                        offer,
                        listingParams,
                        OfferRepositoryEventsImpl(this@MyBidsViewModel, component),
                        this
                    )
                }
            }
        }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            listingBaseViewModel.setListingData(
                listingBaseViewModel.listingData.value.copy(
                    data = LD(
                        filters = OfferFilters.getByTypeFilter(type),
                        methodServer = "get_cabinet_listing_with_my_bids",
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

            val eventParameters = mapOf(
                "user_id" to UserData.login.toString(),
                "profile_source" to "bids"
            )
            analyticsHelper.reportEvent("view_seller_profile", eventParameters)
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
    val viewModel: MyBidsViewModel,
    val component: MyBidsComponent
): OfferRepositoryEvents
{

    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {}

    override fun goToProposalPage(
        offerId: Long,
        type: ProposalType
    ) {}

    override fun goToDynamicSettings(type: String, id: Long) {
        DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
    }

    override fun goToLogin() {
        DefaultRootComponent.Companion.goToLogin(false)
    }

    override fun goToDialog(id: Long?) {
        viewModel.getMainTread {
            component.goToDialog(id)
        }
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {
        viewModel.getMainTread {
            component.goToPurchases()
        }
    }

    override fun goToUserPage(sellerId: Long) {
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
