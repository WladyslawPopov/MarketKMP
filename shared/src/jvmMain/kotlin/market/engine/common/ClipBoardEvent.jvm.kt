package market.engine.common

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun clipBoardEvent(string: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(string)
    clipboard.setContents(selection, null)
}
