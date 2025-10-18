@file:JvmName("Misc")

package com.github.seepick.timesheet.builder

import com.github.seepick.timesheet.OffReason
import com.github.seepick.timesheet.Tag
import java.time.LocalDate
import java.time.Month

/** Construction of model because of invalid DSL definition failed. */
class BuilderException(message: String, cause: Exception? = null) : Exception(message, cause)

interface BuilderEntryFields {
//    val day: LocalDate
}

internal sealed class BuilderEntry : BuilderEntryFields {
    abstract fun matches(date: LocalDate): Boolean
}

internal data class BuilderWorkDayEntry(
    val day: LocalDate,
    val timeRangeSpec: TimeRangeSpec,
    val about: String,
) : BuilderEntry() {
    init {
        if (about.isBlank()) throw BuilderException("An entry's about text must not be blank for entry ${day.toParsableDate()}!")
    }

    val tags: MutableSet<Tag> = mutableSetOf()

    override fun matches(date: LocalDate) = day == date
    override fun toString() = "Day[${day.toParsableDate()}/${timeRangeSpec.toParseableString()} - tags: $tags]"
}

interface ReasonableOffEntry {
    var reason: OffReason?
}

internal data class BuilderDayOffEntry(
    val day: LocalDate,
) : BuilderEntry(), ReasonableOffEntry {
    override var reason: OffReason? = null

    override fun matches(date: LocalDate) = day == date
    override fun toString() = "${this::class.simpleName}[${day.toParsableDate()} - reason: $reason]"
}

internal data class BuilderDaysOffEntry(
    val year: Int,
    val month: Month,
    val days: IntRange,
): BuilderEntry(), ReasonableOffEntry {
    override var reason: OffReason? = null
    val dates = days.map {  day ->
        LocalDate.of(year, month, day)
    }

    override fun matches(date: LocalDate) = dates.any { it == date }
    override fun toString() = "${this::class.simpleName}[$days - reason: $reason]"
}
