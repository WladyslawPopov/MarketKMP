package market.engine.common

import java.awt.Desktop
import java.net.URI

actual fun openEmail(email: String) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.MAIL)) {
            val mailto = URI("mailto:$email")
            desktop.mail(mailto)
        } else {
            println("MAIL action not supported")
        }
    } else {
        println("Desktop API MAIL action not supported")
    }
}
