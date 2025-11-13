package com.github.seepick.timesheet.off

import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.dsl.BuilderEntry
import java.time.LocalDate
import java.time.Month

interface ReasonableOffEntry {
    var reason: OffReason?
}

data class BuilderDayOffEntry(
    val day: LocalDate,
    override var reason: OffReason? = null,
) : BuilderEntry, ReasonableOffEntry {
    override fun matches(date: LocalDate) = day == date
    override fun toString() = "${this::class.simpleName}[${day.toParsableDate()} - reason: $reason]"
}

data class BuilderDaysOffEntry(
    val year: Int,
    val month: Month,
    val days: IntRange,
) : BuilderEntry, ReasonableOffEntry {
    override var reason: OffReason? = null
    val dates = days.map { day ->
        LocalDate.of(year, month, day)
    }

    override fun matches(date: LocalDate) = dates.any { it == date }
    override fun toString() = "${this::class.simpleName}[$days - reason: $reason]"
}
