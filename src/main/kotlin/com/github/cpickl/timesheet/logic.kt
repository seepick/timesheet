package com.github.cpickl.timesheet

interface TimeSheetProvider {
    fun provide(): TimeSheet
}
