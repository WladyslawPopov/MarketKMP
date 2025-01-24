package market.engine.fragments.messenger

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DialogsData
import market.engine.core.data.types.MessageType
import market.engine.core.network.networkObjects.MesImage
import market.engine.core.utils.convertDateYear
import market.engine.core.utils.convertHoursAndMinutes
import market.engine.core.utils.getCurrentDate
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialogItem(
    item: DialogsData.MessageItem,
    modifier: Modifier = Modifier,
    openImage: (Int) -> Unit,
    onLongClick: (DialogsData.MessageItem) -> Unit = {}
) {
    val richText = rememberRichTextState()
    val isIncoming = (item.messageType == MessageType.INCOMING)

    val arrangement = if (isIncoming) Arrangement.Start else Arrangement.End
    val alignment = if (isIncoming) Alignment.Start else Alignment.End

    val bubbleColor = if (isIncoming) {
        colors.transparentGrayColor
    } else {
        colors.outgoingBubble
    }

    val chatBubbleShape = RoundedCornerShape(
        topStart = if (isIncoming) 4.dp else 20.dp ,
        topEnd = 20.dp,
        bottomEnd = if (isIncoming) 20.dp else 4.dp,
        bottomStart = 20.dp
    )

    Row(
        modifier = modifier.fillMaxWidth().padding(dimens.smallPadding),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.6f),
            horizontalAlignment = alignment,
        ) {
            Surface(
                shape = chatBubbleShape,
                color = bubbleColor,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { /* single click */ },
                        onLongClick = { onLongClick(item) }
                    )
            ) {
                Column(
                    modifier = Modifier.padding(dimens.extraSmallPadding),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {
                    if (item.message.isNotBlank()) {
                        richText.setHtml(item.message)
                        Text(
                            text = richText.annotatedString,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.black,
                            modifier = Modifier.padding(dimens.smallPadding)
                        )
                    }

                    if (!item.images.isNullOrEmpty()) {
                        ImagesPreviewGrid(
                            images = item.images!!
                        ){ index ->
                            openImage(index)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
        }
    }
}

@Composable
fun SeparatorDialogItem(
    item: DialogsData.SeparatorItem
){
    val today = stringResource(strings.todayLabel)
    Row(
        modifier = Modifier
            .padding(dimens.mediumPadding)
    ) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        Text(
            text = if(getCurrentDate().convertDateYear() == item.dateTime){
                today
            } else {
                item.dateTime
            },
            style = MaterialTheme.typography.titleSmall,
            color = colors.grayText,
            modifier = Modifier.padding(horizontal = dimens.mediumPadding)
        )
        Divider(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
}


@Composable
fun ImagesPreviewGrid(
    images: List<MesImage>,
    openImage: (Int) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(if(images.size < 3) images.size else 3),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .heightIn(max = 1200.dp)
            .wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        verticalItemSpacing = dimens.extraSmallPadding
    ) {
        items(images.size) { index ->
            val image = images[index]
            AsyncImage(
                model = image.thumbUrl ?: "",
                contentDescription = null,
                imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                    .crossfade(true)
                    .components {
                        add(SvgDecoder.Factory())
                    }.build(),
                Modifier.clickable {
                    openImage(index)
                },
                contentScale = ContentScale.Crop
            )
        }
    }
}
