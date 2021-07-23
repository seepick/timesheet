@file:JvmName("Logic")

package com.github.cpickl.timesheet

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

fun interface TimeSheetProvider {
    fun provide(): TimeSheet
}

interface Clock {
    fun currentLocalDate(): LocalDate
    fun currentLocalDateTime(): LocalDateTime
}

object SystemClock : Clock {
    override fun currentLocalDate(): LocalDate = LocalDate.now()
    override fun currentLocalDateTime(): LocalDateTime = LocalDateTime.now()
}

class TimeCalculator(
    private val clock: Clock = SystemClock
) {
    private val minutesInHour = 60

    fun calculate(sheet: TimeSheet): TimeReport {
        val daysTotal = ChronoUnit.DAYS.between(sheet.startDate, clock.currentLocalDate())
        val daysToWork = 0.rangeTo(daysTotal)
            .map { sheet.startDate.plusDays(it) }
            .filter { it.dayOfWeek.isWeekDay && !sheet.freeDaysContains(it.dayOfWeek) }
            .count() - sheet.entries.dayOffEntries.count()
        val totalMinutesToWork = (daysToWork * sheet.hoursToWorkPerDay * minutesInHour).toLong()
        val totalMinutesWorked = sheet.entries.workEntries.sumOf { it.duration }

        return TimeReport(
            totalMinutesToWork = totalMinutesToWork,
            totalMinutesWorked = totalMinutesWorked,
        )
    }
}
