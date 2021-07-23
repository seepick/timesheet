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

data class TimeReport(
    val totalMinutesToWork: Minutes,
    val totalMinutesWorked: Minutes,
) {
    private val hoursFormatter = DecimalFormat("##.#")

    val balance: Minutes = totalMinutesWorked - totalMinutesToWork
    private val hoursBalance: Double = balance.toDouble() / 60.0
    private val hoursBalanceFormatted = hoursFormatter.format(hoursBalance)
    private val absHoursBalanceFormatted = hoursFormatter.format(abs(hoursBalance))

    val hoursBalanceString =
        if (balance < 0.0) "need to work [$absHoursBalanceFormatted] more hours" else "surplus of [$hoursBalanceFormatted] hours"
}
