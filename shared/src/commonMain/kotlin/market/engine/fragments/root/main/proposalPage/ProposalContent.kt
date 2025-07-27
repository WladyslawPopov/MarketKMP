package market.engine.fragments.root.main.proposalPage

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ProposalType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.DialogsHeader
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.items.ProposalsItemContent
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProposalContent(
    component: ProposalComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.proposalViewModel
    val offer by viewModel.responseGetOffer.collectAsState()
    val type = viewModel.type

    val proposalState = viewModel.body.collectAsState()
    val isLoading by viewModel.isShowProgress.collectAsState()
    val isError by viewModel.errorMessage.collectAsState()

    val scrollState = rememberLazyScrollState(viewModel)

    val focusManager = LocalFocusManager.current

    val toastItem by viewModel.toastItem.collectAsState()

    val subtitle = viewModel.subtitle.collectAsState()

    val initFields = viewModel.responseFields.collectAsState()

    val noFound : (@Composable () -> Unit)? = remember(proposalState.value) {
        if (proposalState.value != null && proposalState.value?.bodyList?.firstOrNull()?.proposals == null && type == ProposalType.ACT_ON_PROPOSAL) {
            {
                NoItemsFoundLayout(
                    icon = drawables.proposalIcon,
                    title = stringResource(strings.notFoundProposalsLabel),
                ) {
                    viewModel.update()
                }
            }
        } else {
            null
        }
    }

    val error : (@Composable () ->Unit)? = remember(isError){
        if (isError.humanMessage.isNotBlank()){
            {
                OnError(
                    isError
                ){
                    viewModel.update()
                }
            }
        }else{
            null
        }
    }

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = SimpleAppBarData(
                    listItems = listOf(
                        NavigationItem(
                            title = "",
                            icon = drawables.recycleIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            hasNews = false,
                            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                            badgeCount = null,
                            onClick = viewModel::update
                        )
                    ),
                    onBackClick = {
                        component.goBack()
                    }
                )
            ){
                TextAppBar(stringResource(strings.proposalTitle))
            }

            val headerItem by viewModel.mesHeaderItem.collectAsState()
            if(headerItem.title.text.isNotBlank()) {
                DialogsHeader(headerItem)
            }
        },
        onRefresh = {
            viewModel.update()
        },
        error = error,
        noFound = noFound,
        isLoading = isLoading,
        toastItem = toastItem,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }.fillMaxSize()
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            state = scrollState.scrollState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            item {
                if(subtitle.value.text != "") {
                    Row(
                        modifier = Modifier
                            .padding(dimens.smallPadding)
                            .background(
                                colors.actionTextColor.copy(alpha = 0.25f),
                                MaterialTheme.shapes.medium
                            ).padding(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            subtitle.value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.darkBodyTextColor
                        )
                    }
                }
            }

            items(proposalState.value?.bodyList ?: emptyList()){ body ->
                ProposalsItemContent(
                    offer,
                    body,
                    type,
                    initFields.value.find { it.first == (body.buyerInfo?.id ?: 0L) }?.second ?: emptyList(),
                    isLoading,
                    onValueChange = {
                        viewModel.onValueChange(it, body.buyerInfo?.id ?: 0L)
                    },
                    confirmProposal = {
                        viewModel.confirmProposal( body.buyerInfo?.id ?: 0L)
                    },
                    goToUser = {
                        component.goToUser(it)
                    }
                )
            }
        }
    }
}
