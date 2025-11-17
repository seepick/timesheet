package com.github.seepick.timesheet.date

import java.time.LocalDate
import java.time.YearMonth

const val MINUTES_IN_HOUR = 60

fun LocalDate.isBeforeOrSame(other: LocalDate) =
    this.isBefore(other) || this == other

fun LocalDate.atStartOfYear(): LocalDate = withDayOfYear(1)
fun LocalDate.atEndOfYear(): LocalDate = withDayOfYear(lengthOfYear())
fun LocalDate.atStartOfMonth(): LocalDate = withDayOfMonth(1)
fun LocalDate.atEndOfMonth(): LocalDate = withDayOfMonth(lengthOfMonth())

fun YearMonth.atBeginOfMonth(): LocalDate = LocalDate.of(year, month, 1)
fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, month)
