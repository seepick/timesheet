package com.github.seepick.timesheet.date

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** inclusive **/
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
) : Iterable<LocalDate> {

    constructor(dates: Pair<String, String>) : this(dates.first.parseDate(), dates.second.parseDate())

    private val asClosedRange: ClosedRange<LocalDate> = startDate..endDate

    init {
        require(startDate.isBeforeOrSame(endDate)) { "Required start [$startDate] <= end [$endDate]" }
    }

    fun contains(date: LocalDate) = asClosedRange.contains(date)

    override fun iterator(): Iterator<LocalDate> =
        List(ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1) { i ->
            startDate.plusDays(i.toLong())
        }.iterator()
}
