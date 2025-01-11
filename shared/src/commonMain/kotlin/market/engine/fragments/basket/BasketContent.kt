package market.engine.fragments.basket

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.BasketItem
import market.engine.core.data.types.WindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.items.BasketItemContent
import org.jetbrains.compose.resources.stringResource


@Composable
fun BasketContent(
    component: BasketComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.basketViewModel
    val userBasket = modelState.value.basketViewModel.responseGetUserCart.collectAsState()
    val isLoading = modelState.value.basketViewModel.isShowProgress.collectAsState()
    val isError = modelState.value.basketViewModel.errorMessage.collectAsState()


    val basketData : MutableState<ArrayList<Pair<Long, ArrayList<BasketItem>>>?> = remember { mutableStateOf(null) }

    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val successToast = stringResource(strings.operationSuccess)

    val columns = remember { if (isBigScreen) 2 else 1 }

    val state = rememberLazyListState()

    val noFound = @Composable {
        if (userBasket.value?.bodyList?.isEmpty() == true){
            showNoItemLayout(
                image = drawables.cartEmptyIcon,
                title = stringResource(strings.cardIsEmptyLabel),
                textButton = stringResource(strings.startShoppingLabel),
            ){
                //go to listing
                component.goToListing()
            }
        }
    }

    val error = @Composable {
        if (isError.value.humanMessage.isNotBlank()){
            onError(
                isError.value
            ){
                viewModel.onError(ServerErrorException())
                viewModel.getUserCart()
            }
        }
    }

    val refresh = {
        viewModel.getUserCart()
    }

    val subtitle : MutableState<String?> = remember {
        mutableStateOf(null)
    }

    val oneOffer = stringResource(strings.oneOfferLabel)
    val manyOffers = stringResource(strings.manyOffersLabel)
    val exManyOffers = stringResource(strings.exManyOffersLabel)

    LaunchedEffect(userBasket.value){
        basketData.value = null
        subtitle.value = null

        if (userBasket.value?.bodyList?.isNotEmpty() == true){
            val count = userBasket.value?.bodyList?.size ?: 0
            subtitle.value = buildString {
                if (count.toString()
                        .matches(Regex("""([^1]1)${'$'}""")) || count == 1
                ) {
                    append("$count $oneOffer")
                } else if (count.toString()
                        .matches(Regex("""([^1][234])${'$'}""")) || count == 2 || count == 3 || count == 4
                ) {
                    append("$count $exManyOffers")
                } else {
                    append("$count $manyOffers")
                }
            }

            userBasket.value?.bodyList?.forEach { item ->
                if (basketData.value?.find { it.first == item.sellerId } == null) {
                    basketData.value?.add(Pair(item.sellerId, arrayListOf()))
                }
            }

            userBasket.value?.bodyList?.forEach { item->
                basketData.value?.filter { it.first == item.sellerId }?.get(0)?.second?.add(
                    BasketItem(false, item.offerId, item)
                )
            }
        }
    }

    BaseContent(
        topBar = {
            BasketAppBar(
                stringResource(strings.yourBasketTitle),
                subtitle.value,
                clearBasket = {
                    viewModel.viewModelScope.launch {
                        val res = viewModel.clearBasket()
                        if (res != null){
                            refresh()
                            viewModel.showToast(
                                successToastItem.copy(message = successToast)
                            )
                        }
                    }
                }
            )
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

            val size = basketData.value?.size ?: 0

            LazyColumn(
                state = state,
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            ) {
                items(size, key = {
                    basketData.value?.get(it)?.first ?: it
                }) { index ->
                    if (index % columns == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            for (columnIndex in 0 until columns) {
                                val itemIndex = index + columnIndex
                                if (itemIndex < size) {
                                    val item = basketData.value?.get(itemIndex)
                                    Box(modifier = Modifier.weight(1f)) {
                                        item?.let {
                                            BasketItemContent(it)
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
