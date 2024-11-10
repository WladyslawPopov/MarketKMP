package market.engine.presentation.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import market.engine.common.ScrollBarsProvider
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.items.ToastItem
import market.engine.core.types.ToastType
import market.engine.widgets.badges.ToastTypeMessage
import market.engine.widgets.buttons.floatingCreateOfferButton

@Composable
fun BaseContent(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    isLoading: State<Boolean>,
    showVerticalScrollbarState: Any? = null,
    isShowFloatingButton: Boolean = false,
    toastItem: MutableState<ToastItem> = remember {
        mutableStateOf(
            ToastItem(
                message = "",
                type = ToastType.WARNING,
                isVisible = false
            )
        )
    },
    topBar: (@Composable () -> Unit) = {},
    bottomBar: (@Composable () -> Unit) = {},
    drawerContent: (@Composable () -> Unit) = {},
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    error: (@Composable () -> Unit)? = null,
    noFound: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
){
    LaunchedEffect(toastItem.value.isVisible) {
        delay(4000)
        toastItem.value = ToastItem(
            message = "",
            type = ToastType.WARNING,
            isVisible = false
        )
    }
    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = drawerContent,
        gesturesEnabled = drawerState.isOpen,
    ) {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = {
                if (isShowFloatingButton) {
                    floatingCreateOfferButton {

                    }
                }
            }
        ) { innerPadding ->
            SwipeRefreshContent(
                isRefreshing = isLoading.value,
                modifier =  modifier.padding(innerPadding).fillMaxSize(),
                onRefresh = {
                    onRefresh()
                },
            ) {
                Box(modifier = modifier.fillMaxSize()){
                    if (noFound != null) {
                        noFound()
                    }

                    if (error != null) {
                        error()
                    }

                    content()

                    if (showVerticalScrollbarState != null) {
                        ScrollBarsProvider().getVerticalScrollbar(
                            showVerticalScrollbarState,
                            modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                        )
                    }

                    if (toastItem.value.isVisible) {
                        val t = toastItem.value
                        ToastTypeMessage(
                            t.isVisible,
                            message = t.message,
                            modifier = Modifier.align(Alignment.TopCenter),
                            toastType = t.type
                        )
                    }
                }
            }
        }
    }
}
