package market.engine.presentation.offer

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import kotlinx.coroutines.launch
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.util.printLogD
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.widgets.exceptions.FullScreenImageViewer
import market.engine.widgets.exceptions.HorizontalImageViewer
import market.engine.widgets.items.PromoOfferRowItem
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OfferContent(
    component: OfferComponent,
    modifier: Modifier
) {
    val mainViewModel: MainViewModel = koinViewModel()

    val model = component.model.subscribeAsState()
    val offerViewModel = model.value.offerViewModel
    val offer = offerViewModel.responseOffer.collectAsState()
    val isLoading = offerViewModel.isShowProgress.collectAsState()

    val isImageViewerVisible = remember { mutableStateOf(false) }

    val scrollState = rememberCoroutineScope()
    val images = offer.value.images?.map { it.urls?.big?.content.orEmpty() } ?: emptyList()
    val pagerState = rememberPagerState(
        pageCount = { images.size},
    )
    val string = remember { mutableStateOf("") }
    val visitedHistory = offerViewModel.responseHistory.collectAsState()

    val ourChoiceList = offerViewModel.responseOurChoice.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.sendEvent(UIMainEvent.UpdateTopBar{
            if (!isImageViewerVisible.value) {
                OfferAppBar(
                    title = stringResource(strings.defaultOfferTitle),
                    isFavorite = offer.value.isWatchedByMe,
                    onFavClick = {

                    },
                    onCartClick = {

                    },
                    onBeakClick = {
                        component.onBeakClick()
                    }
                )
            }
        })
        mainViewModel.sendEvent(UIMainEvent.UpdateFloatingActionButton {})
        mainViewModel.sendEvent(UIMainEvent.UpdateError(null))
        mainViewModel.sendEvent(UIMainEvent.UpdateNotFound(null))
    }

    DisposableEffect(Unit){
        onDispose {
            offerViewModel.addHistory(model.value.id)
        }
    }

    Box(modifier.fillMaxSize()){
        SwipeRefreshContent(
            isRefreshing = isLoading.value,
            modifier = modifier.fillMaxSize(),
            onRefresh = {
                component.updateOffer(model.value.id)
            },
        ) {
            AnimatedVisibility(
                modifier = modifier.fillMaxSize(),
                visible = !isLoading.value,
                enter = expandIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.background(color = colors.primaryColor).fillMaxSize(),
                    contentPadding = PaddingValues(dimens.smallPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
                ) {
                    item {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clickable { isImageViewerVisible.value = true },
                            contentAlignment = Alignment.Center
                        ) {
                           HorizontalImageViewer(
                                images = images,
                                pagerState = pagerState,
                           )
                        }
                    }

                    item {
                        Text(
                            text = offer.value.title ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = dimens.largeText),
                        )
                    }

                    item {
                        string.value = ""

                        val handler = KsoupHtmlHandler
                            .Builder()
                            .onText { text ->
                                string.value += text
                            }
                            .onError { error ->
                                string.value  = "Error: ${error.message}"
                                println("HTML Parse Error: ${error.message}")
                            }
                            .build()

                        val ksoupHtmlParser = KsoupHtmlParser(
                            handler = handler,
                        )

                        TitleText(
                            text = stringResource(strings.description),
                            modifier = Modifier.fillMaxWidth()
                        )

                        ksoupHtmlParser.write(offer.value.description ?: "")
                        ksoupHtmlParser.end()

                        BasicText(
                            text = string.value,
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.smallPadding),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = dimens.extraLargeText),
                            color = {
                                colors.darkBodyTextColor
                            }
                        )
                    }

                    //recommended list offers
                    if (ourChoiceList.value.isNotEmpty()) {
                        item {
                            TitleText(
                                text = stringResource(strings.ourChoice),
                                modifier = Modifier.fillMaxWidth()
                            )
                            LazyRow(
                                modifier = Modifier.height(300.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                items(ourChoiceList.value) { offer ->
                                    PromoOfferRowItem(
                                        offer
                                    ) {
                                        component.navigateToOffers(offer.id)
                                    }
                                }
                            }
                        }
                    }
                    // visited list offers
                    if (visitedHistory.value.isNotEmpty()) {
                        item {
                            TitleText(
                                text = stringResource(strings.lastViewedOffers),
                                modifier = Modifier.fillMaxWidth()
                            )

                            LazyRow(
                                modifier = Modifier.height(300.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                items(visitedHistory.value) { offer ->
                                    PromoOfferRowItem(
                                        offer
                                    ) {
                                        component.navigateToOffers(offer.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isImageViewerVisible.value) {
                FullScreenImageViewer(
                    images = images,
                    initialIndex = pagerState.currentPage,
                    onClose = { exitPage ->
                        scrollState.launch {
                            isImageViewerVisible.value = false
                            pagerState.scrollToPage(exitPage)
                        }
                    }
                )
            }
        }
    }
}
