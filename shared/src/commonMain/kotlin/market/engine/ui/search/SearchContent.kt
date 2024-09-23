package market.engine.ui.search


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.globalObjects.listingData
import market.engine.business.globalObjects.searchData
import market.engine.widgets.common.getCategoryIcon
import market.engine.widgets.exceptions.onError
import market.engine.widgets.pages.BaseContent
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchContent(
    component: SearchComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value

    val isLoading = model.isLoading.collectAsState()
    val isError = model.isError.collectAsState()
    val history = model.history?.collectAsState()

    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        { onError(model.isError.value) { } }
    }else{
        null
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        BaseContent(
            modifier = modifier,
            isLoading = isLoading,
            error = error,
            topBar = {
                SearchAppBar(
                    modifier
                ) {
                    component.onCloseClicked()
                }
            },
            onRefresh = { }
        ){
            LazyColumn(
                modifier = Modifier
                    .heightIn(400.dp,2000.dp)
                    .padding(top = dimens.mediumPadding, bottom = 60.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(history != null) {
                    items(history.value) { history ->

                        Spacer(modifier = Modifier.height(dimens.smallPadding))
                    }
                }
            }
        }
    }
}
