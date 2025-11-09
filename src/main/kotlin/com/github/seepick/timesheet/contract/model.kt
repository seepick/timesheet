package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.WorkDay
import java.time.LocalDate

// TODO @file:JvmName("Model") .. i guess necessary because of class with test?!

data class WorkContract(
    val daysOff: Set<WorkDay>,
    val hoursPerWeek: Int,
) {
    companion object {
        val default = WorkContract(emptySet(), 38)
    }
    val hoursPerDay = hoursPerWeek / (WorkDay.entries.size - daysOff.size).toDouble()
}

data class RangedWorkContract(
    val contract: WorkContract,
    val dateRange: DateRange,
)

data class DefinedWorkContract(
    val contract: WorkContract,
    val definedAt: LocalDate,
)
