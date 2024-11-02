package market.engine.presentation.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import market.engine.common.ScrollBarsProvider
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.core.items.ToastItem
import market.engine.core.navigation.children.ChildMain
import market.engine.core.types.ToastType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.home.DrawerContent
import market.engine.presentation.main.bottomBar
import market.engine.widgets.badges.ToastTypeMessage
import market.engine.widgets.bars.getBottomNavBar
import market.engine.widgets.bars.getRailNavBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import org.jetbrains.compose.resources.stringResource

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
    drawerMethod : (() -> Unit) = {},
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
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
        drawerContent = { DrawerContent(drawerState, scope, modifier, drawerMethod) },
        gesturesEnabled = drawerState.isOpen,
    ) {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar
        ) { innerPadding ->
            SwipeRefreshContent(
                isRefreshing = isLoading.value,
                onRefresh = {
                    onRefresh()
                }
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding))
                {
                    AnimatedVisibility(
                        modifier = modifier,
                        visible = !isLoading.value,
                        enter = expandIn(),
                        exit = fadeOut()
                    ) {
                        if (noFound != null) {
                            noFound()
                        }

                        if (error != null) {
                            error()
                        }

                        content()
                    }

                    if (showVerticalScrollbarState != null) {
                        ScrollBarsProvider().getVerticalScrollbar(
                            showVerticalScrollbarState,
                            modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                        )
                    }

                    if (isShowFloatingButton) {
                        floatingCreateOfferButton(
                            modifier.align(Alignment.BottomEnd)
                                .padding(bottom = dimens.largePadding, end = dimens.smallPadding)
                        ) {

                        }
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


