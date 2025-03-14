package market.engine.widgets.ilustrations

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun FullScreenImageViewer(
    pagerFullState: PagerState,
    images: List<String>,
    isUpdate: Boolean = false,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var guestActivate by remember { mutableStateOf(false) }

    LaunchedEffect(pagerFullState.targetPage){
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = Modifier
            .animateContentSize()
            .background(colors.grayLayout.copy(alpha = 0.8f))
    ) {
        HorizontalImageViewer(
            images = images,
            pagerState = pagerFullState,
            isUpdate = isUpdate,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1f) 1f else 3.5f
                            offset = Offset.Zero
                            guestActivate = !guestActivate
                        }
                    )
                }.pointerInput(guestActivate){
                    if (guestActivate) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                val newOffset = offset + pan
                                offset = Offset(
                                    x = newOffset.x.coerceIn(
                                        -(size.width * (scale - 1)),
                                        size.width * (scale - 1)
                                    ),
                                    y = newOffset.y.coerceIn(
                                        -(size.height * (scale - 1)),
                                        size.height * (scale - 1)
                                    )
                                )
                            } else {
                                offset = Offset.Zero
                                guestActivate = false
                            }
                        }
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}


