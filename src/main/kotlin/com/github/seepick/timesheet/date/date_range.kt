package com.github.seepick.timesheet.date

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** inclusive **/
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
) : Iterable<LocalDate> {

    val asClosedRange: ClosedRange<LocalDate> = startDate..endDate

    init {
        require(startDate.isBeforeOrSame(endDate))
    }

    override fun iterator(): Iterator<LocalDate> =
        List(ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1) { i ->
            startDate.plusDays(i.toLong())
        }.iterator()
}
