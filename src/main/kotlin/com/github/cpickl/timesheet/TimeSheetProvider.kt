package com.github.cpickl.timesheet

/** Main entrance point. */
interface TimeSheetProvider {
    fun provide(): TimeSheet
    fun chooseReport(report: ReportContext)
}
