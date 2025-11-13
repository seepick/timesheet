@file:JvmName("Logic")

package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.Clock
import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.MINUTES_IN_HOUR
import com.github.seepick.timesheet.date.SystemClock
import com.github.seepick.timesheet.date.isWeekDay
import com.github.seepick.timesheet.off.DayOffEntry
import com.github.seepick.timesheet.report.TimeReportData
import com.github.seepick.timesheet.timesheet.TimeSheet
import java.time.LocalDate
import kotlin.math.roundToLong

class ReportCalculator(
    private val clock: Clock = SystemClock
) {

    fun calculate(sheet: TimeSheet) = TimeReportData(
        sheet = sheet,
        totalMinutesToWork = sheet.calcTotalMinutesToWork(),
        totalMinutesWorked = sheet.entries.workEntries.sumOf { it.duration },
    )

    private fun TimeSheet.calcTotalMinutesToWork(): Long {
        println("${this.startDate} -> ${clock.currentLocalDate()}")
        val dayOffEntries = this.entries.filterIsInstance<DayOffEntry>().map { it.day }.toSet()
        val totalHoursToWork = DateRange(this.startDate, clock.currentLocalDate())
            .mapNotNull { this.hoursToWorkOrNull(it, dayOffEntries)?.also { println("Foo: $it") } }
            .sum()
        return (totalHoursToWork * MINUTES_IN_HOUR).roundToLong()
    }

    private fun TimeSheet.hoursToWorkOrNull(currentDate: LocalDate, dayOffEntries: Set<LocalDate>): Double? {
        if (!currentDate.dayOfWeek.isWeekDay) return null
        val contract = this.contractFor(currentDate)
        if (contract.javaDaysOff.contains(currentDate.dayOfWeek)) return null
        if (dayOffEntries.contains(currentDate)) return null
        return contract.hoursPerDay
    }
}
