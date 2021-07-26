package com.github.cpickl.timesheet.report

import com.github.cpickl.timesheet.Reporter
import com.github.cpickl.timesheet.TimeReportData

class NotificationReporter : Reporter {
    override fun report(data: TimeReportData) {
        val balanceString = TextualReporterUtil.generateHoursBalanceString(data) { this }
        Notifier().display(Notification("Timesheet", balanceString))
    }
}
private class Notifier {

//    private val log = logger {}
    private val os = OsSniffer.os
    private var warningPrinted = false

    fun display(notification: Notification) {
        if (os == Os.Mac) {
            ProcessExecuterImpl.execute(
                command = "osascript",
                args = listOf("-e", "display notification \"${notification.message}\" with title \"${notification.title}\""),
                context = ExecuteContext(
                    suppressOutput = true
                )
            )
        } else if (!warningPrinted) {
            warningPrinted = true
//            log.warn { "Your operating system is not supported for notifications :(" }
        }
    }

}

data class Notification(
    val title: String,
    val message: String
)
