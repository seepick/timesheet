package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.WorkDay
import com.github.seepick.timesheet.dsl.TimesheetAppDsl

@TimesheetAppDsl
interface ContractDsl {
    var hoursPerWeek: Int
    var dayOff: WorkDay
    var daysOff: Set<WorkDay>
    fun noDaysOff()
}
