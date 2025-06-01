package market.engine.widgets.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.center
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun rememberTooltipState(): TooltipState = remember { TooltipState() }

@Stable
class TooltipState internal constructor() {

    // параметры блока-обертки
    fun hide() {
        isVisible = false
    }

    fun show() {
        isVisible = true
    }

    // длина оборачивающего блока. Нужна для того, чтобы определить максимальную ширину тултипа
    internal var tooltipWrapperWidth: Int by mutableIntStateOf(0)
    // информация о лейауте для оборачивающего блока. Понадобится нам, когда будем считать смещение тултипа
    private var tooltipWrapperLayoutCoordinated: LayoutCoordinates? = null


    // параметры тултипа

    // данные для отображения в тултипе: заголовок, сабтайтл
    internal var data: TooltipData? by mutableStateOf(null)
    // видим ли тултип в данный момент
    internal var isVisible: Boolean by mutableStateOf(false)
    // итоговое смещение тултипа
    internal var tooltipOffset: IntOffset by mutableStateOf(IntOffset.Zero)
    // информация о лейауте тултипа. Понадобится нам, когда будем считать смещение тултипа
    private var tooltipLayoutCoordinates: LayoutCoordinates? = null
    // смещение пипочки тултипа
    private var triangleXOffset: Int = 0


    // параметры якорного блока

    // информация о лейауте якорного элемента. Понадобится нам, когда будем считать смещение
    internal var anchorLayoutCoordinates: LayoutCoordinates? by mutableStateOf(null)

    internal fun initialize(
        data: TooltipData,
        initialVisibility: Boolean,
    ) {
        this.data = data

        if (initialVisibility) {
            show()
        }
    }

    internal fun changeTooltipWrapperLayoutCoordinates(layoutCoordinates: LayoutCoordinates) {
        tooltipWrapperLayoutCoordinated = layoutCoordinates
        tooltipWrapperWidth = layoutCoordinates.size.width

        syncTooltipOffset()
    }

    internal fun changeAnchorLayoutCoordinates(layoutCoordinates: LayoutCoordinates) {
        anchorLayoutCoordinates = layoutCoordinates

        syncTooltipOffset()
    }

    internal fun changeTooltipLayoutCoordinates(layoutCoordinates: LayoutCoordinates) {
        tooltipLayoutCoordinates = layoutCoordinates

        syncTooltipOffset()
    }

    private fun syncTooltipOffset() {
        val tooltipWrapperLC = tooltipWrapperLayoutCoordinated ?: return

        // смещение тултипа посередине якорного
        val anchorWidgetDisplacement = anchorLayoutCoordinates?.let { anchorLC ->

            // позиция опорного элемента в координатах блока TooltipWrapper. Объяснение ниже
            val parent = tooltipWrapperLC.localPositionOf(anchorLC, Offset.Zero)
            val size = anchorLC.size

            val x = parent.x + size.width / 2f
            val y = parent.y + size.height
            IntOffset(
                x = x.toInt(),
                y = y.toInt() + TRIANGLE_HEIGHT.toInt(),
            )
        } ?: IntOffset.Zero

        // собственное смещение тултипа на половину ширины тултипа
        val properDisplacement = tooltipLayoutCoordinates?.let {
            IntOffset(it.size.center.x, 0)
        } ?: IntOffset.Zero

        val tooltipWidth = tooltipLayoutCoordinates?.size?.width ?: 0
        // левая верхняя точка тултипа
        val newTopLeftOffset = anchorWidgetDisplacement - properDisplacement
        // правая верхняя точка тултипа
        val newTopRightOffset = newTopLeftOffset + IntOffset(tooltipWidth, 0)


        // Нужно учесть кейсы, если наш тултип вышел за пределы блока и смещать тултип таким образом, чтобы он помещался
        val resultDependWindowBoundaries = when {
            // кейс, когда тултип выходит за левую границу
            newTopLeftOffset.x < 0 -> {
                triangleXOffset = newTopLeftOffset.x
                IntOffset(0, newTopLeftOffset.y)
            }


            // Кейс, когда тултип выходит за правую границу
            newTopRightOffset.x > tooltipWrapperWidth -> {
                triangleXOffset = newTopRightOffset.x - tooltipWrapperWidth
                IntOffset(tooltipWrapperWidth - tooltipWidth, newTopRightOffset.y)
            }


            // Кейс, когда тултип не выходит за границы
            else -> {
                triangleXOffset = 0
                newTopLeftOffset
            }
        }

        tooltipOffset = resultDependWindowBoundaries
    }

    internal fun getTrianglePath(): Path = Path().apply {
        val triangleHeight = TRIANGLE_HEIGHT
        val triangleWidth = TRIANGLE_WIDTH

        val tooltipLayoutCoordinates = tooltipLayoutCoordinates ?: return@apply

        val widgetSize = tooltipLayoutCoordinates.size

        val offsetToCenterX = widgetSize.center.x.toFloat() + triangleXOffset
        val offsetToCenterY = 0f

        moveTo(offsetToCenterX, offsetToCenterY - triangleHeight)
        lineTo(offsetToCenterX - triangleWidth / 2f, offsetToCenterY)
        lineTo(offsetToCenterX + triangleWidth / 2f, offsetToCenterY)
        close()
    }

    private companion object {
        const val TRIANGLE_WIDTH = 40f
        const val TRIANGLE_HEIGHT = 40f
    }
}

@Stable
data class TooltipData(
    val title: String?,
    val subtitle: String,
    val dismissIconResource: DrawableResource?
)
