package market.engine.common

import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKitUI.EKEventEditViewAction
import platform.EventKitUI.EKEventEditViewController
import platform.EventKitUI.EKEventEditViewDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.Foundation.NSDate
import platform.Foundation.earlierDate
import platform.Foundation.laterDate
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual fun openCalendarEvent(description: String) {
    val eventStore = EKEventStore()

    eventStore.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, error ->
        dispatch_async(dispatch_get_main_queue()) {
            if (granted) {
                val event = EKEvent.eventWithEventStore(eventStore).apply {
                    title = "Calendar Event"
                    notes = description
                    val now = NSDate()
                    startDate = now.earlierDate(NSDate())
                    endDate = now.laterDate(NSDate())
                    calendar = eventStore.defaultCalendarForNewEvents
                }

                val editVC = EKEventEditViewController().apply {
                    this.event = event
                    this.eventStore = eventStore
                    this.editViewDelegate = object : NSObject(), EKEventEditViewDelegateProtocol {
                        override fun eventEditViewController(
                            controller: EKEventEditViewController,
                            didCompleteWithAction: EKEventEditViewAction
                        ) {
                            controller.dismissViewControllerAnimated(true, completion = null)
                        }
                    }
                }

                val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
                rootVC?.presentViewController(editVC, animated = true, completion = null)
            } else {
                println("Calendar access not granted: ${error?.localizedDescription}")
            }
        }
    }
}
