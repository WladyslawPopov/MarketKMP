package market.engine.fragments.root.main.messenger

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.common.openUrl
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.DialogsData
import market.engine.core.data.types.MessageType
import market.engine.core.utils.convertHoursAndMinutes
import market.engine.core.utils.parseDeepLink
import market.engine.widgets.grids.ImagesPreviewGrid
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialogItem(
    item: DialogsData.MessageItem,
    modifier: Modifier = Modifier,
    openImage: (Int) -> Unit,
    onMenuClick: (String) -> Unit,
    goToOffer: (Long) -> Unit,
    goToListing: (Long) -> Unit,
    goToUser: (Long) -> Unit
) {
    val richText = rememberRichTextState()
    val isIncoming = (item.messageType == MessageType.INCOMING)

    val arrangement = if (isIncoming) Arrangement.Start else Arrangement.End
    val alignment = if (isIncoming) Alignment.Start else Alignment.End

    val bubbleColor = if (isIncoming) {
            colors.incomingBubble
        } else {
            colors.outgoingBubble
        }


    val showMenu = remember { mutableStateOf(false) }

    val listDialogOperations =
        listOf(
            "copy" to stringResource(strings.actionCopy),
            "delete" to stringResource(strings.actionDelete)
        )


    val chatBubbleShape = remember {
        RoundedCornerShape(
            topStart = if (isIncoming) 4.dp else 20.dp,
            topEnd = 20.dp,
            bottomEnd = if (isIncoming) 20.dp else 4.dp,
            bottomStart = 20.dp
        )
    }

    var url = remember { "" }
    val text = remember { richText.setHtml(item.message).annotatedString.text }
    val annotatedString = remember {
        buildAnnotatedString {
            append(text)
            val urlPattern = Regex("${SAPI.SERVER_BASE}[\\w./?=#-]+")
            urlPattern.findAll(text).forEach { matchResult ->
                url = matchResult.value
                val start = matchResult.range.first
                val end = matchResult.range.last + 1

                addStringAnnotation(
                    tag = "URL",
                    annotation = url,
                    start = start,
                    end = end
                )

                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = colors.brightBlue
                    ),
                    start = start,
                    end = end
                )
            }
        }
    }

    val modClick = remember {
        if (url.isNotBlank()) {
            Modifier.clickable {
                when (val deepLink = parseDeepLink(url)) {
                        is DeepLink.GoToOffer -> {
                            goToOffer(deepLink.offerId)
                        }

                        is DeepLink.GoToListing -> {
                            goToListing(deepLink.ownerId)
                        }

                        is DeepLink.GoToUser -> {
                            goToUser(deepLink.userId)
                        }

                        is DeepLink.GoToAuth -> {
                            openUrl(url)
                        }

                        is DeepLink.GoToDialog -> {
                            openUrl(url)
                        }

                        is DeepLink.GoToDynamicSettings -> {
                            openUrl(url)
                        }

                        DeepLink.GoToRegistration -> {
                            openUrl(url)
                        }

                        is DeepLink.GoToVerification -> {
                            openUrl(url)
                        }

                        is DeepLink.Unknown -> {
                            openUrl(url)
                        }

                        null -> {}
                    }
            }
        }else{
            Modifier
        }
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(dimens.smallPadding),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalAlignment = alignment,
        ) {
            Surface(
                shape = chatBubbleShape,
                color = bubbleColor,
            ) {
                Column(
                    modifier = Modifier.combinedClickable(
                        onClick = { /* single click */ },
                        onLongClick = { showMenu.value = true }
                    ).padding(dimens.extraSmallPadding),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.black,
                        modifier = modClick.padding(dimens.smallPadding)
                    )

                    if (!item.images.isNullOrEmpty()) {
                        ImagesPreviewGrid(
                            images = item.images!!
                        ){ index ->
                            openImage(index)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!item.readByReceiver) {
                            Text(
                                text = stringResource(strings.notReadMessagesLabel),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.notifyTextColor,
                                modifier = Modifier.padding(horizontal = dimens.smallPadding)
                            )
                        }

                        Text(
                            text = item.dateTime.toString().convertHoursAndMinutes(),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.grayText,
                            modifier = Modifier.padding(horizontal = dimens.extraSmallPadding)
                        )
                    }
                }
            }

            DropdownMenu(
                modifier = modifier.widthIn(max = 350.dp).heightIn(max = 400.dp),
                expanded = showMenu.value,
                onDismissRequest = { showMenu.value = false },
                containerColor = colors.white,
                offset = DpOffset(40.dp, 0.dp)
            ) {
                listDialogOperations.forEach { action ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = action.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.black
                            )
                        },
                        onClick = {
                            onMenuClick(action.first)
                        }
                    )
                }
            }
        }
    }
}
