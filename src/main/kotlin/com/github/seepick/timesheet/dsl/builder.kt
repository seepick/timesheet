package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.date.TimeRangeSpec
import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.tags.Tag
import java.time.LocalDate

interface BuilderEntryFields {
//    val day: LocalDate
}

interface BuilderEntry : BuilderEntryFields {
    fun matches(date: LocalDate): Boolean
}

data class BuilderWorkDayEntry(
    val day: LocalDate,
    val timeRangeSpec: TimeRangeSpec,
    val about: String,
) : BuilderEntry {
    init {
        if (about.isBlank()) throw InvalidSheetException("An entry's about text must not be blank for entry ${day.toParsableDate()}!")
    }

    val tags: MutableSet<Tag> = mutableSetOf()

    override fun matches(date: LocalDate) = day == date
    override fun toString() = "Day[${day.toParsableDate()}/${timeRangeSpec.toParseableString()} - tags: $tags]"
}
