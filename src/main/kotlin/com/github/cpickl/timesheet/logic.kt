package com.github.cpickl.timesheet

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
    fun foo(sheet: TimeSheet): TimeReport {
        val daysTotal = ChronoUnit.DAYS.between(sheet.startDate, clock.currentLocalDate())
        println("daysTotal: $daysTotal")
        val daysToWork = 0.rangeTo(daysTotal).map { sheet.startDate.plusDays(it) }
            .filter { it.dayOfWeek.isWeekDay && !sheet.daysOffContains(it.dayOfWeek) }.count()
        println("daysToWork: $daysToWork")
        val totalMinutesToWork = daysToWork * sheet.hoursToWorkPerDay * minutesInHour
        println("totalMinutesToWork: $totalMinutesToWork")
        val totalMinutesWorked = sheet.entries.workEntries.sumOf { it.duration }
        println("totalMinutesWorked: $totalMinutesWorked")
        val balance: Minutes = totalMinutesWorked - totalMinutesToWork
        return TimeReport(
            sheet = sheet,
            balance = balance,
        )
    }
}

data class TimeReport(
    val sheet: TimeSheet,
    val balance: Minutes,
) {
    private val hoursFormatter = DecimalFormat("##.#")

    val hoursBalance: Double = balance.toDouble() / 60.0
    val hoursBalanceFormatted = hoursFormatter.format(hoursBalance)
    val hoursBalanceString =
        if (balance < 0.0) "need to work $hoursBalanceFormatted more hours" else "you have surplus of $hoursBalanceFormatted hours"
}
