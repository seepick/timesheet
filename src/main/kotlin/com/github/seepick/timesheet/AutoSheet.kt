package com.github.seepick.timesheet

import com.github.seepick.timesheet.report.ReportContext

/** Main entrance point. */
interface AutoSheet {
    fun provide(): TimeSheet
    fun chooseReport(report: ReportContext)
}
