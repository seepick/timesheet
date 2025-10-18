package com.github.seepick.timesheet

import com.github.seepick.timesheet.report.ReportContext

/** Main entrance point. */
interface AutoSheet {
    fun provide(): com.github.seepick.timesheet.TimeSheet
    fun chooseReport(report: ReportContext)
}
