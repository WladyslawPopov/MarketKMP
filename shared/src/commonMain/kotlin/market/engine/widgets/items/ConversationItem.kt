package market.engine.widgets.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.common.setShortcutForDialog
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.states.CabinetConversationsItemState
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.badges.getBadge
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource


@Composable
fun ConversationItem(
    data: CabinetConversationsItemState,
    updateItem: Long? = null,
    selected : Boolean = false,
    onSelected : (Long) -> Unit = {}
) {
    val conversation = data.conversation
    val events = data.events


    LaunchedEffect(updateItem) {
        if (updateItem == conversation.id){
            events.updateItem()
        }
    }

    if (conversation.interlocutor != null) {
        Card(
            colors = colors.cardColors,
            shape = MaterialTheme.shapes.small,
        ) {
            Row(
                modifier = Modifier.combinedClickable(
                    onClick = {
                        setShortcutForDialog(conversation)
                        events.goToMessenger()
                    },
                    onLongClick = {
                        onSelected(conversation.id)
                    }
                ).fillMaxWidth().padding(dimens.smallPadding),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    AnimatedVisibility(
                        selected,
                        enter = expandIn(),
                        exit = fadeOut()
                    ) {
                        ThemeCheckBox(
                            isSelected = selected,
                            onSelectionChange = {
                                onSelected(conversation.id)
                            },
                            modifier = Modifier
                        )
                    }

                    val imageUser = conversation.interlocutor?.avatar?.thumb?.content
                    if (imageUser != null) {
                        Card(
                            modifier = Modifier.padding(dimens.extraSmallPadding),
                            shape = CircleShape
                        ) {
                            LoadImage(
                                url = imageUser,
                                isShowLoading = false,
                                isShowEmpty = false,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = conversation.interlocutor?.login ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.black,
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            if (conversation.countUnreadMessages > 0) {
                                Icon(
                                    painterResource(drawables.newMessageIcon),
                                    contentDescription = null,
                                    tint = colors.notifyTextColor,
                                )
                            }

                            Text(
                                text = conversation.newMessage ?: "...",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.black,
                                maxLines = 2,
                                minLines = 2,
                            )
                        }

                        if (conversation.countUnreadMessages > 0) {
                            getBadge(conversation.countUnreadMessages, false)
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    horizontalAlignment = Alignment.End
                ) {
                    LoadImage(
                        url = conversation.aboutObjectIcon?.small?.content ?: "",
                        isShowLoading = false,
                        isShowEmpty = true,
                        modifier = Modifier.size(40.dp)
                    )

                    Text(
                        conversation.newMessageTs?.convertDateWithMinutes() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.black,
                    )
                }
            }
        }
    }
}
