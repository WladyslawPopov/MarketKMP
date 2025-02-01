package market.engine.widgets.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.mohamedrejeb.richeditor.model.RichTextState
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.widgets.buttons.RichTextStyleButton


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RichTextStyleRow(
    modifier: Modifier = Modifier,
    state: RichTextState,
) {
    FlowRow (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        RichTextStyleButton(
            onClick = {
                if (state.currentParagraphStyle.textAlign != TextAlign.Left) {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Left
                        )
                    )
                } else {
                    state.removeParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Left
                        )
                    )
                }
            },
            isSelected = state.currentParagraphStyle.textAlign == TextAlign.Left,
            icon = drawables.alignLeftIcon
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentParagraphStyle.textAlign != TextAlign.Center) {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                } else {
                    state.removeParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                }

            },
            isSelected = state.currentParagraphStyle.textAlign == TextAlign.Center,
            icon = drawables.alignCenterIcon
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentParagraphStyle.textAlign != TextAlign.Right) {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Right
                        )
                    )
                }else{
                    state.removeParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Right
                        )
                    )
                }
            },
            isSelected = state.currentParagraphStyle.textAlign == TextAlign.Right,
            icon = drawables.alignRightIcon
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentSpanStyle.fontWeight != FontWeight.Bold) {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }else{
                    state.removeSpanStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
            icon = drawables.textBoldIcon
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentSpanStyle.fontStyle != FontStyle.Italic) {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    )
                }else{
                    state.removeSpanStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            },
            isSelected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
            icon = drawables.textItalicIcon
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) != true) {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }else{
                    state.removeSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            },
            isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
            icon = drawables.textUnderlineIcon
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) != true) {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }else{
                    state.removeSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }
            },
            isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
            icon = drawables.textCrossIcon
        )

        val largeText = MaterialTheme.typography

        RichTextStyleButton(
            onClick = {
                if (state.currentSpanStyle.fontSize != largeText.titleLarge.fontSize) {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontSize = largeText.titleLarge.fontSize,
                        )
                    )
                }else{
                    state.removeSpanStyle(
                        SpanStyle(
                            fontSize = largeText.titleLarge.fontSize,
                        )
                    )
                }
            },
            isSelected = state.currentSpanStyle.fontSize == largeText.titleLarge.fontSize,
            icon = drawables.textBigIcon
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentSpanStyle.color != Color.Red) {
                    state.toggleSpanStyle(
                        SpanStyle(
                            color = Color.Red
                        )
                    )
                } else {
                    state.removeSpanStyle(
                        SpanStyle(
                            color = Color.Red
                        )
                    )
                }
            },
            isSelected = state.currentSpanStyle.color == Color.Red,
            icon = drawables.fillCircleIcon,
            tint = Color.Red
        )

        RichTextStyleButton(
            onClick = {
                if (state.currentSpanStyle.background != Color.Yellow) {
                    state.toggleSpanStyle(
                        SpanStyle(
                            background = Color.Yellow
                        )
                    )
                }else{
                    state.removeSpanStyle(
                        SpanStyle(
                            background = Color.Yellow
                        )
                    )
                }
            },
            isSelected = state.currentSpanStyle.background == Color.Yellow,
            icon = drawables.recordCircleIcon,
            tint = Color.Yellow
        )

        RichTextStyleButton(
            onClick = {
                state.toggleUnorderedList()
            },
            isSelected = state.isUnorderedList,
            icon = drawables.listCheckIcon,
        )

        RichTextStyleButton(
            onClick = {
                state.toggleOrderedList()
            },
            isSelected = state.isOrderedList,
            icon = drawables.listOrderedIcon,
        )
    }
}
