package market.engine.core.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import market.engine.core.globalData.ThemeResources.colors

fun String.parseColorString(): Color {
    return try {
        when {
            this.startsWith("#") -> {
                // Ensure the hex color string is in the correct format
                val hexColor = this.replace("#", "").toLongOrNull(16)
                if (hexColor != null) {
                    // Add alpha channel if it's missing (default is fully opaque)
                    if (this.length == 7) {
                        Color(hexColor or 0xFF000000) // Add alpha for #RRGGBB format
                    } else {
                        Color(hexColor) // Use as-is for #AARRGGBB format
                    }
                } else {
                    colors.black
                }
            }
            this.startsWith("rgb") -> {
                // Parse RGB format (e.g., rgb(255, 0, 0))
                val rgb = this.removePrefix("rgb(").removeSuffix(")").split(",")
                if (rgb.size == 3) {
                    val r = rgb[0].trim().toInt()
                    val g = rgb[1].trim().toInt()
                    val b = rgb[2].trim().toInt()
                    Color(r, g, b)
                } else {
                   colors.black
                }
            }
            else -> colors.black
        }
    } catch (e: Exception) {
        colors.black
    }
}

fun String.parseHtmlToAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        // Stack to track pushed styles
        val pushedStylesStack = mutableListOf<String>()

        // Track the currently active ParagraphStyle
        var currentParagraphStyle: String? = null

        val handler = KsoupHtmlHandler
            .Builder()
            // Handles plain text content inside the HTML tags
            .onText { text ->
                append(text) // Append the text directly to the AnnotatedString
            }
            // Handles the opening of HTML tags
            .onOpenTag { name, attrs, _ ->
                when (name.lowercase()) {
                    // Bold text
                    "b", "strong" -> {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Italic text
                    "i", "em" -> {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Underlined text
                    "u" -> {
                        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Strikethrough text
                    "s", "del" -> {
                        pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Anchor tag as styled text
                    "a" -> {
                        val backgroundColor = attrs["style"]?.extractCssProperty("background-color")?.parseColorString() ?: colors.transparent
                        pushStyle(
                            SpanStyle(
                                color = colors.black,
                                background = backgroundColor
                            )
                        )
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Highlight text
                    "mark" -> {
                        pushStyle(SpanStyle(background = Color.Yellow))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Small text
                    "small" -> {
                        pushStyle(SpanStyle(fontSize = 10.sp))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Subscript text
                    "sub" -> {
                        pushStyle(SpanStyle(baselineShift = BaselineShift.Subscript))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Superscript text
                    "sup" -> {
                        pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Font color
                    "font" -> {
                        val color = attrs["color"]?.parseColorString() ?: Color.Black
                        pushStyle(SpanStyle(color = color))
                        pushedStylesStack.add(name.lowercase())
                    }
                    // Span with inline styles
                    "span" -> {
                        val backgroundColor = attrs["style"]?.extractCssProperty("background-color")?.parseColorString()
                        if (backgroundColor != null) {
                            pushStyle(
                                SpanStyle(
                                    color = colors.darkBodyTextColor,
                                    background = backgroundColor
                                )
                            )
                            pushedStylesStack.add(name.lowercase())
                        }
                    }
                    // Paragraphs and divs with alignment
                    "p", "div" -> {
                        // Handle ParagraphStyle to avoid overlap
                        if (currentParagraphStyle != null) {
                            // Add a line break to end the current paragraph
                            append("\n")
                            pop() // Close the previous ParagraphStyle
                        }
                        val alignment = when (attrs["style"]?.extractCssProperty("text-align")) {
                            "center" -> TextAlign.Center
                            "right" -> TextAlign.Right
                            else -> TextAlign.Left
                        }
                        pushStyle(ParagraphStyle(textAlign = alignment))
                        currentParagraphStyle = name.lowercase() // Track active ParagraphStyle
                    }
                    // Line breaks
                    "br" -> {
                        append("\n") // Add a new line
                    }
                    // List item
                    "li" -> {
                        append("• ") // Add bullet for list item
                    }
                }
            }
            // Handles the closing of HTML tags
            .onCloseTag { name, _ ->
                val tag = name.lowercase()
                if (tag == currentParagraphStyle) {
                    // Close the active ParagraphStyle
                    append("\n") // Add a line break to end the paragraph
                    pop()
                    currentParagraphStyle = null
                } else if (pushedStylesStack.contains(tag)) {
                    pop() // Only pop if the tag was previously pushed
                    pushedStylesStack.remove(tag) // Remove the tag from the stack
                }
            }
            // Handles errors during HTML parsing
            .onError { error ->
                println("HTML Parse Error: ${error.message}")
            }
            .build()

        // Parse the HTML content using the configured KsoupHtmlHandler
        val ksoupHtmlParser = KsoupHtmlParser(handler = handler)
        ksoupHtmlParser.write(this@parseHtmlToAnnotatedString) // Process the input string
        ksoupHtmlParser.end() // Finish parsing
    }
}

fun String.extractCssProperty(propertyName: String): String? {
    val regex = Regex("$propertyName:\\s*([^;]+);?")
    return regex.find(this)?.groupValues?.get(1)?.trim()
}
