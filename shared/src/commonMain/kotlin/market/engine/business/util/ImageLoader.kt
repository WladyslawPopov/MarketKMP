package market.engine.business.util

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import market.engine.business.core.network.APIService
import io.ktor.util.InternalAPI
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import market.engine.common.toImageBitmap
import org.koin.compose.koinInject

@OptIn(InternalAPI::class)
@Composable
fun getImage(url : String) {
    val getClient : APIService = koinInject()
    val imageState = remember { mutableStateOf<ImageBitmap?>(null) }

    CoroutineScope(Dispatchers.IO).launch {
        val response = getClient.getImage(url)
        val imageBitmap = toImageBitmap(response.content.readRemaining().readBytes())

        imageState.value = imageBitmap
    }

    loadImage(imageState.value)
}

@Composable
fun loadImage(image: ImageBitmap?){
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null
        )
    }
}
