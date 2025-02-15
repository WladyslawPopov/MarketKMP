package market.engine.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.ui.graphics.Color

interface Colors {
    val selected : Color
    val primaryColor : Color
    val titleTextColor: Color
    val inactiveBottomNavIconColor: Color
    val ratingBlue: Color
    val promoHighlight: Color
    val buyNowColor: Color
    val auctionWithBuyNow: Color
    val outgoingBubble: Color
    val incomingBubble: Color
    val actionTextColor: Color
    val black: Color
    val transparent: Color
    val notifyTextColor: Color
    val white: Color
    val lightGray: Color
    val grayLayout: Color
    val grayText: Color
    val errorLayoutBackground: Color
    val greenWaterBlue: Color
    val positiveGreen: Color
    val negativeRed: Color
    val textA0AE: Color
    val alwaysWhite: Color
    val rippleColor: Color
    val solidGreen: Color
    val yellowSun: Color
    val crimsonRed: Color
    val steelBlue: Color
    val transparentGrayColor: Color
    val brightGreen: Color
    val brightPurple: Color
    val brightBlue: Color

    val themeButtonColors : ButtonColors
    val simpleButtonColors : ButtonColors
    val actionButtonColors : ButtonColors
    val navItemColors : NavigationBarItemColors
    val navRailItemColors : NavigationRailItemColors
    val cardColors : CardColors
    val cardColorsPromo : CardColors
    val darkBodyTextColor : Color
}
