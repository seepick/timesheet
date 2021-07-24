package com.github.cpickl.timesheet

/** Main entrance point. */
interface AutoSheet {
    fun provide(): TimeSheet
    fun chooseReport(report: ReportContext)
}
