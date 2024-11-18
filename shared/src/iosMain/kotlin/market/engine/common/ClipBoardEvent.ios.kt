package market.engine.common

import platform.UIKit.UIPasteboard


actual fun clipBoardEvent(string: String) {
    UIPasteboard.generalPasteboard.string = string
}
