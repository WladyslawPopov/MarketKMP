package market.engine.widgets.items

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.items.PhotoTemp
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.exceptions.LoadImage

@Composable
fun PhotoCard(
    item: PhotoTemp,
    viewModel: BaseViewModel,
    onItemUploaded: (String) -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier
) {
    val isLoading = remember { mutableStateOf(false) }

    LaunchedEffect(item.uri) {
        if (item.tempId == null && item.id == null && item.uri != null) {
            isLoading.value = true
            val newTempId = viewModel.uploadFile(item)
            isLoading.value = false

            if (newTempId != null) {
                onItemUploaded(newTempId)
            }
        }
    }

    Card(
        onClick = {},
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Row {
            if (isLoading.value) {
                Text("Loading...")
            } else {
                LoadImage(item.uri ?: "", size = 96.dp)
            }
        }
    }
}
