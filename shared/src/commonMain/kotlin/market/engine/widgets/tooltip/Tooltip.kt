package market.engine.widgets.tooltip

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun Tooltip(state: TooltipState, onClick: MutableState<() -> Unit>) {
   val data = state.data

   val animatedTriangleVisibility by animateFloatAsState(
       targetValue = if (state.isVisible) 1f else 0f,
       animationSpec = tween(300)
   )

   // Если тултип не виден или для него нет данных или якорного элемента, то ничего не рисуем
   if (animatedTriangleVisibility == 0f || data == null || state.anchorLayoutCoordinates == null) return 

   // высчитываем максимальную ширину тултипа. В данном случае будет 70% от ширины блока для тултипов
   val maxTooltipWidth = LocalDensity.current.run { 
     (state.tooltipWrapperWidth * .7f).toDp()
   }

   Column(
       modifier = Modifier
           .widthIn(max = maxTooltipWidth)
           // смещаем тултип на расчитанное в стейте значение
           .offset { state.tooltipOffset }
           // передаем информацию о лейауте тултипа
           .onGloballyPositioned { state.changeTooltipLayoutCoordinates(it) }
           // рисуем пипочку сверху тултипа
           .drawBehind {
               val path = state.getTrianglePath()
               drawPath(
                   path = path,
                   alpha = animatedTriangleVisibility,
                   color = colors.white,
               )
           }
           // управляем прозрачностью тултипа для плавных действий над ним
           .graphicsLayer { alpha = animatedTriangleVisibility }
           .clickable(onClick = onClick.value)
           .clip(MaterialTheme.shapes.medium)
           .background(colors.white)
           .padding(dimens.mediumPadding),
   ) {
       Row {
           if (data.title != null) {
               Text(
                   data.title,
                   color = colors.black,
                   style = MaterialTheme.typography.titleSmall,
                   modifier = Modifier.weight(1f)
               )
           }

           if (data.dismissIcon != null) {
               Icon(
                   modifier = Modifier.size(dimens.extraSmallIconSize),
                   painter = painterResource(data.dismissIcon),
                   contentDescription = "",
                   tint = Color.White,
               )
           }
       }

       Text(
           text = data.subtitle,
           color = MaterialTheme.colorScheme.onPrimary
       )
   }
}
