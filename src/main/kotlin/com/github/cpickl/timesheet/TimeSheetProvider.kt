package com.github.cpickl.timesheet

/** Main entrance point. */
fun interface TimeSheetProvider {
    fun provide(): TimeSheet
}
