package market.engine.core.data.items

import androidx.compose.ui.text.AnnotatedString

data class MesHeaderItem(
    val title : AnnotatedString,
    val subtitle : AnnotatedString,
    val image : String?,
    val onClick : () -> Unit
)
