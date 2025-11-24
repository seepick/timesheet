@file:JvmName("TagsCalculator")

package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.report.TagsReport
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.timesheet.TimeSheet

fun calculateTags(reportView: ReportView, sheet: TimeSheet): TagsReport {
    val result = mutableMapOf<Tag, Long>()
    sheet.entries.workEntries.filter(reportView::filter).forEach { entry ->
        entry.tags.forEach { tag ->
            result[tag] = result.getOrDefault(tag, 0) + entry.duration
        }
    }
    return TagsReport(result)
}
