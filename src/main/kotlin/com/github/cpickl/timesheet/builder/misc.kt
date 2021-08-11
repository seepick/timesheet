@file:JvmName("Misc")

package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.OffReason
import com.github.cpickl.timesheet.Tag
import java.time.LocalDate

/** Construction of model because of invalid DSL definition failed. */
class BuilderException(message: String, cause: Exception? = null) : Exception(message, cause)

interface BuilderEntryFields {
    val day: LocalDate
}

internal sealed class BuilderEntry : BuilderEntryFields

internal data class BuilderWorkDayEntry(
    override val day: LocalDate,
    val timeRangeSpec: TimeRangeSpec,
    val about: String,
) : BuilderEntry() {
    init {
        if (about.isBlank()) throw BuilderException("An entry's about text must not be blank for entry ${day.toParsableDate()}!")
    }

    val tags: MutableSet<Tag> = mutableSetOf()

    override fun toString() = "Day[${day.toParsableDate()}/${timeRangeSpec.toParseableString()} - tags: $tags]"
}

internal data class BuilderDayOffEntry(
    override val day: LocalDate,
) : BuilderEntry() {
    var reason: OffReason? = null

    override fun toString() = "DayOff[${day.toParsableDate()} - reason: $reason]"
}
