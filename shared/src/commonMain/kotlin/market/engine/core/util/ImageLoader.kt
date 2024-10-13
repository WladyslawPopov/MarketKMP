package market.engine.core.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import market.engine.core.network.APIService
import io.ktor.util.InternalAPI
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import market.engine.common.toImageBitmap
import market.engine.core.constants.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(InternalAPI::class)
@Composable
fun getImage(url : String?, size: Dp = 300.dp) {
    val getClient : APIService = koinInject()
    val imageState = remember { mutableStateOf<ImageBitmap?>(null) }

    CoroutineScope(Dispatchers.IO).launch {
        val response = getClient.getImage(url ?: "")
        val imageBitmap = toImageBitmap(response.content.readRemaining().readBytes())

        imageState.value = imageBitmap
    }

    loadImage(imageState.value, size)
}

@Composable
fun loadImage(image: ImageBitmap?, size : Dp){
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    }else{
        Image(
            painter = painterResource(drawables.noImageOffer),
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    }
}
