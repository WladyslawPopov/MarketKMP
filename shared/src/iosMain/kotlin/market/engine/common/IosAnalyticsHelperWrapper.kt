import platform.darwin.NSObject
import platform.Foundation.NSDictionary
import platform.Foundation.NSSelectorFromString
import kotlinx.cinterop.*
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.util.printLogD
import platform.Foundation.NSString

object IosAnalyticsHelperWrapper : AnalyticsHelper {
    var swiftHelper: NSObject? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun initialize() {
        swiftHelper?.performSelector(NSSelectorFromString("initialize"))
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun reportEvent(eventName: String, eventParameters: Map<String, Any?>) {
        // Wrap the eventName and eventParameters into an NSDictionary
        val eventInfo = mapOf(
            "eventName" to eventName,
            "parameters" to eventParameters.toNSDictionary()
        ).toNSDictionary()

        // Call the Swift method with the NSDictionary
        if (swiftHelper?.respondsToSelector(NSSelectorFromString("reportEventWithDictionary:")) == true) {
            swiftHelper?.performSelector(
                NSSelectorFromString("reportEventWithDictionary:"),
                withObject = eventInfo
            )
        } else {
            println("Method reportEventWithDictionary: not found")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun reportEvent(eventName: String, eventParameters: String) {
        // Wrap the eventName and eventParameters into an NSDictionary
        val eventInfo = mapOf(
            "eventName" to eventName,
            "parameters" to eventParameters
        ).toNSDictionary()

        printLogD("reportEvent", "eventInfo: $eventInfo")

        if (swiftHelper?.respondsToSelector(NSSelectorFromString("reportEventWithDictionary:")) == true) {
            swiftHelper?.performSelector(
                NSSelectorFromString("reportEventWithDictionary:"),
                withObject = eventInfo
            )
        } else {
            println("Method reportEventWithDictionary: not found")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun updateUserProfile(attributes: Map<String, Any?>) {
        val nsAttributes = attributes.toNSDictionary()

        // Call performSelector with user attributes
        swiftHelper?.performSelector(
            NSSelectorFromString("updateUserProfile:"),
            withObject = nsAttributes
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun setUserProfileID(userID: Long) {
        val nsUserID = userID.toString() as NSString

        // Call performSelector with userID
        swiftHelper?.performSelector(
            NSSelectorFromString("setUserProfileID:"),
            withObject = nsUserID
        )
    }

    // Helper function to convert Kotlin Map to NSDictionary
    private fun Map<String, Any?>.toNSDictionary(): NSDictionary {
        val dictionary = mutableMapOf<NSString, Any?>()
        this.forEach { (key, value) ->
            dictionary[key as NSString] = value
        }
        return dictionary as NSDictionary
    }
}
