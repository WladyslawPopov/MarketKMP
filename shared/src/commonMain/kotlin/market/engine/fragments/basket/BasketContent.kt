package market.engine.fragments.basket

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.User
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
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

    val basketData : MutableState<List<Pair<User?, List<Offer?>>>> = remember { mutableStateOf(emptyList()) }

    val listOffers = remember { mutableStateOf(emptyList<Long>()) }

    val successToast = stringResource(strings.operationSuccess)

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItem.value
    )

    LaunchedEffect(state){
        snapshotFlow {
            state.firstVisibleItemIndex
        }.collect {
            viewModel.firstVisibleItem.value = it
        }
    }

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

    LaunchedEffect(userBasket){
        snapshotFlow {
            userBasket.value
        }.collect { ub->
            if (ub?.bodyList?.isNotEmpty() == true){
                viewModel.setLoading(true)
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

                val groupedBySeller = ub.bodyList.groupBy { it.sellerId }

                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    val result = groupedBySeller.map { (sellerId, items) ->
                        val sellerUser: User? = viewModel.getUser(sellerId)
                        val basketItems: List<Offer?> = items.map { item ->
                            Offer(
                                id = item.offerId,
                                title = item.offerTitle,
                                currentPricePerItem = item.offerPrice,
                                currentQuantity = item.availableQuantity,
                                quantity = item.quantity,
                                sellerData = User(id = item.sellerId),
                                freeLocation = item.freeLocation,
                                externalUrl = item.offerImage,
                                safeDeal = item.isBuyable ?: false
                            )
                        }

                        sellerUser to basketItems
                    }
                    basketData.value = result
                    viewModel.setLoading(false)
                }
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

            LazyColumn(
                state = state,
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            ) {
                items(basketData.value.size, key = { basketData.value[it].first?.id ?: it }) { index ->
                    Spacer(modifier = Modifier.height(dimens.smallSpacer))
                    BasketItemContent(
                        basketData.value[index],
                        goToUser = { userId ->
                            component.goToUser(userId)
                        },
                        goToOffer = { offerId ->
                            component.goToOffer(offerId)
                        },
                        changeQuantity = { offerId, quantity ->
                            viewModel.viewModelScope.launch {
                                val bodyAddB = HashMap<String,String>()
                                bodyAddB["offer_id"] = offerId.toString()
                                bodyAddB["quantity"] = quantity.toString()

                                val res = viewModel.addOfferToBasket(bodyAddB, offerId)

                                if (res != null){
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
                            }
                        },
                        deleteOffer = { offerId ->
                            listOffers.value = buildList {
                                add(offerId)
                            }
                        },
                        clearUserOffers = { offers ->
                            listOffers.value = buildList {
                                addAll(offers)
                            }
                        }
                    )
                }
            }
            if (listOffers.value.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { listOffers.value = emptyList() },
                    title = { },
                    text = {
                        Text(
                            if (listOffers.value.size == 1){
                                stringResource(strings.warningDeleteOfferBasket)
                            }else{
                                stringResource(strings.warningDeleteSelectedOfferFromBasket)
                            }
                        )
                    },
                    confirmButton = {
                        SimpleTextButton(
                            text = stringResource(strings.acceptAction),
                            backgroundColor = colors.textA0AE,
                            onClick = {
                                val offerIds = arrayListOf<JsonPrimitive>()

                                listOffers.value.forEach {
                                    offerIds.add(JsonPrimitive(it))
                                }

                                val jsonArray = JsonArray(offerIds)

                                val body = JsonObject(
                                    mapOf(
                                        "offer_ids" to jsonArray
                                    )
                                )

                                viewModel.viewModelScope.launch {
                                    val res = viewModel.deleteItem(
                                        body,
                                        userBasket.value?.bodyList?.first(),
                                        userBasket.value?.bodyList?.first()?.sellerId ?: 1L
                                    )
                                    if (res != null){
                                        refresh()
                                        viewModel.showToast(
                                            successToastItem.copy(message = successToast)
                                        )
                                    }
                                }

                                listOffers.value = emptyList()
                            }
                        )
                    },
                    dismissButton = {
                        SimpleTextButton(
                            text = stringResource(strings.closeWindow),
                            backgroundColor = colors.inactiveBottomNavIconColor,
                            onClick = {
                                listOffers.value = emptyList()
                            }
                        )
                    }
                )
            }
        }
    }
}
