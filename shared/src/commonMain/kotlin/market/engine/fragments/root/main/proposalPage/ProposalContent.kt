package market.engine.fragments.root.main.proposalPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProposalContent(
    component: ProposalComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.proposalViewModel
    val proposalState = modelState.value.proposalViewModel.responseGetProposal.collectAsState()
    val isLoading = modelState.value.proposalViewModel.isShowProgress.collectAsState()
    val isError = modelState.value.proposalViewModel.errorMessage.collectAsState()

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItem.value
    )
    val subtitle : MutableState<String?> = remember {
        mutableStateOf(null)
    }

    val oneOffer = stringResource(strings.oneOfferLabel)
    val manyOffers = stringResource(strings.manyOffersLabel)
    val exManyOffers = stringResource(strings.exManyOffersLabel)

    LaunchedEffect(UserData.userInfo){
        val countOffers = UserData.userInfo?.countOffersInCart
        subtitle.value = buildString {
            if (countOffers.toString()
                    .matches(Regex("""([^1]1)${'$'}""")) || countOffers == 1
            ) {
                append("$countOffers $oneOffer")
            } else if (countOffers.toString()
                    .matches(Regex("""([^1][234])${'$'}""")) || countOffers == 2 || countOffers == 3 || countOffers == 4
            ) {
                append("$countOffers $exManyOffers")
            } else {
                append("$countOffers $manyOffers")
            }
        }
    }

    LaunchedEffect(state){
        snapshotFlow {
            state.firstVisibleItemIndex
        }.collect {
            viewModel.firstVisibleItem.value = it
        }
    }

    val refresh = {
        viewModel.onError(ServerErrorException())
        viewModel.getProposal(modelState.value.offerId)
    }

    val noFound = @Composable {
        if (proposalState.value == null){
            showNoItemLayout(
                image = drawables.cartEmptyIcon,
                title = stringResource(strings.cardIsEmptyLabel),
                textButton = stringResource(strings.startShoppingLabel),
            ){
                refresh()
            }
        }
    }

    val error = @Composable {
        if (isError.value.humanMessage.isNotBlank()){
            onError(
                isError
            ){
                refresh()
            }
        }
    }

    BaseContent(
        topBar = {
            ProposalAppBar()
        },
        onRefresh = {
            refresh()
        },
        error = error,
        noFound = noFound,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                state = state,
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimens.smallPadding)
            ) {

            }
        }
    }
}
