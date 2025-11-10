package com.github.seepick.timesheet.date

import java.time.LocalDate

/** inclusive **/
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
)
