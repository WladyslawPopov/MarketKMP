package market.engine.common

import android.content.Intent
import android.net.Uri

actual fun openEmail(email: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
    appContext?.startActivity(intent)
}
