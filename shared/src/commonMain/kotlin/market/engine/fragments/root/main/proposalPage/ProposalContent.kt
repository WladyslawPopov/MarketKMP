package market.engine.fragments.root.main.proposalPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.constants.countProposalMax
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.MesHeaderItem
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.getOfferImagePreview
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.fragments.root.main.messenger.DialogsHeader
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProposalContent(
    component: ProposalComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.proposalViewModel
    val type = modelState.value.proposalType
    val offerState = viewModel.responseGetOffer.collectAsState()
    val proposalState = viewModel.responseGetProposal.collectAsState()
    val fieldsState = viewModel.responseGetFields.collectAsState()
    val isLoading = viewModel.isShowProgress.collectAsState()
    val isError = viewModel.errorMessage.collectAsState()

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItem.value
    )

    val subtitle : MutableState<AnnotatedString> = remember {
        mutableStateOf(buildAnnotatedString {  })
    }

    val makeSubLabel = stringResource(strings.subtitleProposalCountLabel)
    val offerLeftLabel = stringResource(strings.subtitleOfferCountLabel)
    val countsSign = stringResource(strings.countsSign)

    LaunchedEffect(proposalState.value){
        if (offerState.value != null) {
            proposalState.value?.bodyList?.firstOrNull()?.let { prs ->
                subtitle.value = buildAnnotatedString {
                    when (type) {
                        ProposalType.ACT_ON_PROPOSAL -> {
                            if (offerState.value != null) {
                                append(offerLeftLabel)
                                append(" ")
                                withStyle(
                                    SpanStyle(
                                        color = colors.titleTextColor,
                                        fontWeight = FontWeight.Bold,
                                    )
                                ) {
                                    append(offerState.value?.currentQuantity.toString())
                                }
                                append(" ")
                                append(countsSign)
                            }
                        }

                        ProposalType.MAKE_PROPOSAL -> {
                            val countP =
                                countProposalMax - (prs.proposals?.filter { !it.isResponserProposal }?.size
                                    ?: 0)
                            append(makeSubLabel)

                            withStyle(
                                SpanStyle(
                                    color = colors.titleTextColor,
                                    fontWeight = FontWeight.Bold,
                                )
                            ) {
                                append(" ")
                                append(countP.toString())
                            }
                        }
                    }
                }
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
        viewModel.getProposal(modelState.value.offerId, modelState.value.proposalType)
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

    val offer = offerState.value
    val bodyList = proposalState.value?.bodyList

    if (offer != null && bodyList != null) {
        BaseContent(
            topBar = {
                ProposalAppBar(subtitle.value){
                    component.goBack()
                }
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
            Column(modifier = Modifier.fillMaxSize()) {
                val mesHed = MesHeaderItem(
                    title = buildAnnotatedString {
                        append(offer.title)
                    },
                    subtitle = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = colors.titleTextColor
                            )
                        ) {
                            append(offer.currentPricePerItem.toString())
                            append(stringResource(strings.currencySign))
                        }
                    },
                    image = offerState.value?.getOfferImagePreview(),
                ) {
                    component.goToOffer(offer.id)
                }

                DialogsHeader(
                    mesHed
                )

                bodyList.forEach { body ->
                    ProposalItem(
                        body,
                        type,
                        fieldsState.value?.fields
                    ){
                        refresh()
                    }
                }
            }
        }
    }
}
