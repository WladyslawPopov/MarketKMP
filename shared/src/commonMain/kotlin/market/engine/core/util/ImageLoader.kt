package market.engine.core.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.ktor.client.statement.readRawBytes
import market.engine.core.network.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import market.engine.common.toImageBitmap
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun getImage(url : String?, size: Dp = 300.dp, showEmpty: Boolean = true) {
    val getClient : APIService = koinInject()
    val imageState = remember { mutableStateOf<ImageBitmap?>(null) }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = getClient.getImage(url ?: "")
            val imageBitmap = toImageBitmap(response.readRawBytes())
            imageState.value = imageBitmap
        } catch ( e : Exception) {
            printLogD("Load Image Error", e.message ?: "")
        }
    }

    if (imageState.value != null) {
        Image(
            bitmap = imageState.value!!,
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    }else{
        if (showEmpty) {
            Image(
                painter = painterResource(drawables.noImageOffer),
                contentDescription = null,
                modifier = Modifier.size(size),
                colorFilter = ColorFilter.tint(colors.grayLayout)
            )
        }
    }
}
