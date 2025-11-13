package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.WorkDay
import java.time.LocalDate

data class WorkContract(
    val daysOff: Set<WorkDay>,
    val hoursPerWeek: Int,
) {
    companion object {
        val default = WorkContract(emptySet(), 40)
    }

    val javaDaysOff = daysOff.map { it.javaDay }
    val hoursPerDay = hoursPerWeek / (5 - daysOff.size).toDouble()
}

data class RangedWorkContract(
    val contract: WorkContract,
    /** considering only its entries (<=), not the actual clock date */
    val dateRange: DateRange,
) {
    fun isWithin(date: LocalDate) =
        dateRange.asClosedRange.contains(date)
}

data class DefinedWorkContract(
    val contract: WorkContract,
    val definedAt: LocalDate,
)
