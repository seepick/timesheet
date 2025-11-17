@file:JvmName("TagsCalculator")

package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.report.TagsReport
import com.github.seepick.timesheet.timesheet.TimeSheet

fun calculateTags(reportView: ReportView, sheet: TimeSheet): TagsReport {
//    val entries: List<WorkDayEntry> = sheet.entries.workEntries.filter(reportRange::filter)
//    if (sheet.entries.workEntries.isEmpty()) {
    return TagsReport(emptyMap())
//    }
//    return TagsReport(
    // FIXME tag report

//        mapOf(
//            sheet.entries.workEntries.first().tags to sheet.entries.workEntries.sumOf { it.duration }
//        )
//    )
}
