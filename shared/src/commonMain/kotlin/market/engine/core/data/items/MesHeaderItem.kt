package market.engine.core.data.items

import androidx.compose.ui.text.AnnotatedString

data class MesHeaderItem(
    val title : AnnotatedString = AnnotatedString(""),
    val subtitle : AnnotatedString = AnnotatedString(""),
    val image : String? = null,
    val onClick : () -> Unit = {}
)
