package market.engine.fragments.root.main.profile.conversations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationItem(
    conversation: Conversations,
    isVisibleCBMode : Boolean = false,
    isSelected: Boolean = false,
    updateTrigger: Int,
    onSelectionChange: (Boolean) -> Unit,
    goToMessenger: () -> Unit,
) {
    if (updateTrigger < 0) return

    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.small,
    ){
        Row(
            modifier = Modifier.combinedClickable(
                onClick = {
                    goToMessenger()
                },
                onLongClick = {
                    onSelectionChange(!isSelected)
                }
            ).fillMaxWidth().padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                AnimatedVisibility(
                    isVisibleCBMode,
                    enter = expandIn(),
                    exit = fadeOut()
                ) {
                    ThemeCheckBox(
                        isSelected = isSelected,
                        onSelectionChange = onSelectionChange,
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
                            size = 40.dp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.8f).padding(dimens.smallPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = conversation.interlocutor?.login ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.black,
                    )

                    Spacer(Modifier.width(dimens.mediumSpacer))

                    Text(
                        conversation.newMessageTs.toString().convertDateWithMinutes(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.black,
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.black,
                            maxLines = 2,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }


                    if (conversation.countUnreadMessages > 0) {
                        Badge {
                            Text(
                                text = conversation.countUnreadMessages.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            LoadImage(
                url = conversation.aboutObjectIcon?.small?.content ?: "",
                isShowLoading = false,
                isShowEmpty = true,
                size = 40.dp
            )
        }
    }
}
