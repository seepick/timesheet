package com.github.seepick.timesheet.date

import java.time.LocalDate

const val MINUTES_IN_HOUR = 60

fun LocalDate.isBeforeOrSame(other: LocalDate) =
    this.isBefore(other) || this == other
