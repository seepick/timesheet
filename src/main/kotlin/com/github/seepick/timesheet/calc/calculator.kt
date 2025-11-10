@file:JvmName("Logic")

package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.Clock
import com.github.seepick.timesheet.date.SystemClock
import com.github.seepick.timesheet.date.isWeekDay
import com.github.seepick.timesheet.report.TimeReportData
import com.github.seepick.timesheet.timesheet.TimeSheet
import java.time.temporal.ChronoUnit

class ReportCalculator(
    private val clock: Clock = SystemClock
) {
    companion object {
        private const val MINUTES_IN_HOUR = 60
    }

    fun calculate(sheet: TimeSheet): TimeReportData {
        val daysTotal = ChronoUnit.DAYS.between(sheet.startDate, clock.currentLocalDate())
        val daysToWork = 0.rangeTo(daysTotal)
            .map { sheet.startDate.plusDays(it) }
            .filter { it.dayOfWeek.isWeekDay
//                    && !sheet.freeDaysContains(it.dayOfWeek) // FIXME !!! implement contract thing
            }
            .count() - sheet.entries.dayOffEntries.count()
        val totalMinutesToWork = (daysToWork * sheet.hoursToWorkPerDay * MINUTES_IN_HOUR).toLong()
        val totalMinutesWorked = sheet.entries.workEntries.sumOf { it.duration }

        return TimeReportData(
            sheet = sheet,
            totalMinutesToWork = totalMinutesToWork,
            totalMinutesWorked = totalMinutesWorked,
        )
    }
}
