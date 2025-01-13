package market.engine.fragments.createOrder

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseContent

@Composable
fun CreateOrderContent(
    component: CreateOrderComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOrderViewModel

    val focusManager = LocalFocusManager.current


    val refresh = {

    }

    val isLoading = viewModel.isShowProgress.collectAsState()
    val error : (@Composable () -> Unit)? = null

    val state = rememberLazyListState()

    BaseContent(
        topBar = {
            CreateOrderAppBar(
                onBackClick = {
                    component.onBackClicked()
                }
            )
        },
        onRefresh = {
            refresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()
    ) {
//        if (createOfferResponse.value?.status == "operation_success") {
//
//        }else {
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    },
            ) {

                //create btn
                item {

//                    AcceptedPageButton(
//                        text = "",
//                        modifier = Modifier.fillMaxWidth()
//                            .padding(dimens.mediumPadding),
//                    ) {
//
//                    }
                }
            }
//        }
    }
}

fun createJsonBody(
    fields: List<Fields>,
) : JsonObject {
    return buildJsonObject {
        fields.forEach { data ->
            when (data.key) {
                else -> {
                    put(data.key ?: "", data.data!!)
                }
            }
        }
    }
}





