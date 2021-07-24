@file:JvmName("Logic")

package com.github.cpickl.timesheet

import java.time.temporal.ChronoUnit

class TimeCalculator(
    private val clock: Clock = SystemClock
) {
    private val minutesInHour = 60

    fun calculate(sheet: TimeSheet): TimeReportData {
        val daysTotal = ChronoUnit.DAYS.between(sheet.startDate, clock.currentLocalDate())
        val daysToWork = 0.rangeTo(daysTotal)
            .map { sheet.startDate.plusDays(it) }
            .filter { it.dayOfWeek.isWeekDay && !sheet.freeDaysContains(it.dayOfWeek) }
            .count() - sheet.entries.dayOffEntries.count()
        val totalMinutesToWork = (daysToWork * sheet.hoursToWorkPerDay * minutesInHour).toLong()
        val totalMinutesWorked = sheet.entries.workEntries.sumOf { it.duration }

        return TimeReportData(
            sheet = sheet,
            totalMinutesToWork = totalMinutesToWork,
            totalMinutesWorked = totalMinutesWorked,
        )
    }
}
