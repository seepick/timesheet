package com.github.seepick.timesheet.date

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** inclusive **/
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
) : Iterable<LocalDate> {

    companion object {}

    private val asClosedRange: ClosedRange<LocalDate> = startDate..endDate

    init {
        require(startDate.isBeforeOrSame(endDate)) { "Required start [$startDate] <= end [$endDate]" }
    }

    fun contains(date: LocalDate) = asClosedRange.contains(date)

    override fun iterator(): Iterator<LocalDate> =
        List(ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1) { i ->
            startDate.plusDays(i.toLong())
        }.iterator()

    fun limitedBy(other: DateRange) =
        DateRange(
            startDate = if (other.startDate > startDate) other.startDate else startDate,
            endDate = if (other.endDate < endDate) other.endDate else endDate,
        )

    fun limitedEndBy(other: LocalDate) = DateRange(
        startDate = startDate,
        endDate = if (other < endDate) other else endDate,
    )

}
